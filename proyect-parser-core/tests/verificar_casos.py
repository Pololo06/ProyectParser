"""Compila el core y compara su salida PlantUML con los casos de referencia."""

import argparse
import os
from pathlib import Path
import re
import subprocess
import tempfile
from difflib import unified_diff


ROOT = Path(__file__).resolve().parents[2]
CORE = ROOT / "proyect-parser-core"
CASE_ROOT = ROOT / "casos-generador"
DECLARATION = re.compile(r'^\s+(abstract class|class|interface|record|enum|annotation) (.+) \{$')
EDGE = re.compile(
    r'^(\w[\w.]*)(?: ("[^"]+"))? (<\|--|<\|\.\.|\.\.>|-->|\+--|\*--|o--)'
    r'(?: ("[^"]+"))? (\w[\w.]*)(?: : (.*))?$'
)
MEMBER = re.compile(r'^\s+([+~#-])\s*(.*)$')
MODIFIER = re.compile(r'\{(static|abstract|readOnly|final|default)(?:, (static|abstract|readOnly|final|default))?\}')


def split_parameters(text):
    depth = 0
    start = 0
    for index, char in enumerate(text):
        if char == '<':
            depth += 1
        elif char == '>':
            depth -= 1
        elif char == ',' and depth == 0:
            yield text[start:index].strip()
            start = index + 1
    if text.strip():
        yield text[start:].strip()


def compact_type(text):
    return re.sub(r'\s+', '', text)


def member_signature(kind, owner, visibility, body, expected):
    body, separator, initializer = body.partition(' = ')
    modifiers = tuple(sorted(token for match in MODIFIER.findall(body) for token in match if token))
    body = MODIFIER.sub('', body).strip()
    if expected:
        method = re.fullmatch(r'(\w+)\((.*)\) : (.+)', body)
        if method:
            name, raw_parameters, data_type = method.groups()
            data_type = 'constructor' if data_type == 'void <<Create>>' else data_type
            parameters = [parameter.split(' : ', 1) for parameter in split_parameters(raw_parameters)]
        else:
            name, data_type = body.split(' : ', 1)
            parameters = []
            if kind == 'enum' and data_type == owner and modifiers == ('readOnly', 'static'):
                return owner, name, 'enum', (), '', (), ''
    else:
        method = re.fullmatch(r'(?:(.+) )?(\w+)\((.*)\)', body)
        if method:
            data_type, name, raw_parameters = method.groups()
            data_type = data_type or 'constructor'
            parameters = [parameter.rsplit(' ', 1)[::-1] for parameter in split_parameters(raw_parameters)]
        else:
            data_type, name = body.rsplit(' ', 1)
            parameters = []
    return (owner, name, compact_type(data_type),
            tuple((parameter, compact_type(dtype)) for parameter, dtype in parameters),
            visibility, modifiers, initializer if separator else '')


def extract(source, expected):
    types = {}
    members = set()
    edges = set()
    owner = None
    for line in source.splitlines():
        declaration = DECLARATION.match(line)
        if declaration:
            kind, display = declaration.groups()
            if display.startswith('"'):
                display = display.split('"', 2)[1]
            stereotypes = set(re.findall(r'<<(.*?)>>', display))
            display = display.split(' <<', 1)[0]
            name, _, generics = display.partition('<')
            owner = name.strip()
            if owner in types:
                raise ValueError(f"Nombre simple repetido en un caso: {owner}")
            if 'Record' in stereotypes:
                kind = 'record'
            types[owner] = (kind, compact_type(generics), stereotypes - {'Record', 'Abstract', 'Interface'})
            continue
        if line.strip() == '}':
            owner = None
        edge = EDGE.fullmatch(line)
        if edge:
            origin, left, arrow, right, target, label = edge.groups()
            edges.add((origin.rsplit('.', 1)[-1], left or '', arrow, right or '',
                       target.rsplit('.', 1)[-1], label or ''))
        if owner is None:
            continue
        kind, _, stereotypes = types[owner]
        stereotype = re.fullmatch(r'\s*<<(.*?)>>', line)
        if stereotype:
            stereotypes.add(stereotype[1])
        member = MEMBER.match(line)
        if member:
            members.add(member_signature(kind, owner, *member.groups(), expected))
        elif kind == 'enum' and re.fullmatch(r'\s+\w+', line):
            members.add((owner, line.strip(), 'enum', (), '', (), ''))
    return types, members, edges


def generate(output):
    classpath = str(CORE / 'lib' / '*')
    sources = sorted((CORE / 'src').rglob('*.java'))
    with tempfile.TemporaryDirectory(prefix='core-java-') as classes:
        subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-cp', classpath,
                        '-d', classes, *sources, CORE / 'tests' / 'GenerarCasos.java'],
                       cwd=ROOT, check=True)
        run = subprocess.run(['java', '-cp', os.pathsep.join((classes, classpath)), 'GenerarCasos', output],
                             cwd=ROOT, check=True, text=True, capture_output=True)
    (output / 'ejecucion.log').write_text(run.stdout, encoding='utf-8')


def comparison(output, report):
    cases = sorted(CASE_ROOT.glob('caso_[0-9][0-9]'))
    if len(cases) != 25:
        raise ValueError(f"Se esperaban 25 casos y se encontraron {len(cases)}")
    log = (output / 'ejecucion.log').read_text(encoding='utf-8')
    parsed = dict(re.findall(r'^(caso_\d+) parsed (\d+/\d+), failed 0(?: \[\])?$', log, re.M))
    rows = []
    details = []
    totals = [0, 0, 0, 0]
    passes = 0
    diff_dir = output / 'diffs'
    diff_dir.mkdir(exist_ok=True)

    for case in cases:
        name = case.name
        expected = (case / 'src' / 'clases.puml').read_text(encoding='utf-8')
        actual = (output / f'{name}.puml').read_text(encoding='utf-8')
        diff = unified_diff(expected.splitlines(True), actual.splitlines(True),
                            f'{name}/src/clases.puml', f'generado/{name}.puml')
        (diff_dir / f'{name}.diff').write_text(''.join(diff), encoding='utf-8')
        expected_types, expected_members, expected_edges = extract(expected, True)
        actual_types, actual_members, actual_edges = extract(actual, True)
        matches = (expected_types == actual_types, expected_members == actual_members,
                   expected_edges == actual_edges, expected == actual)
        totals = [total + match for total, match in zip(totals, matches)]
        files = parsed.get(name, 'error')
        success = files != 'error' and all(matches[:3])
        passes += success
        checks = ['Sí' if match else 'No' for match in matches]
        rows.append(f"| {name} | {files} | {' | '.join(checks)} | {'Pasa' if success else 'Falla'} |")
        if success:
            continue
        details.append(f'### {name}\n')
        for label, before, after in (
            ('Tipos', {str((n, k, g, sorted(s))) for n, (k, g, s) in expected_types.items()},
             {str((n, k, g, sorted(s))) for n, (k, g, s) in actual_types.items()}),
            ('Miembros', expected_members, actual_members),
            ('Relaciones', expected_edges, actual_edges),
        ):
            for kind, values in (('esperado y ausente', before - after), ('generado adicional', after - before)):
                for value in sorted(values):
                    details.append(f'- {label}, {kind} · `{value}`')
        details.append('')

    parsed_total = sum(int(files.split('/')[0]) for files in parsed.values())
    text = f"""# Informe de casos del generador PlantUML

**{passes}/{len(cases)} casos pasan la comparación de contenido.** Coinciden los tipos en
{totals[0]}/25 casos, los miembros en {totals[1]}/25 y las relaciones en {totals[2]}/25.
Se analizaron {parsed_total} archivos Java en las ejecuciones sin errores.
Hay {totals[3]}/25 coincidencias textuales con saltos de línea normalizados.

La prueba usa `DiagramFacade.generatePlantUml` con las opciones predeterminadas y excluye
`DiagramaClases`, como hacen los proyectos de referencia. Un caso pasa si todos sus archivos
Java se analizan y coinciden sus tipos (nombre simple, categoría, genéricos y estereotipos),
sus miembros (tipos, firmas, visibilidad, modificadores mostrados e inicializadores) y sus
relaciones (extremos, flecha, multiplicidad y etiqueta). Se normalizan las dos sintaxis de
miembros, los estereotipos redundantes con la categoría, los nombres calificados de extremos,
el orden y los espacios de tipos. Los paquetes de presentación, el orden y los estilos visuales
se pueden revisar en los diffs textuales que deja el comando.

Desde la raíz del repositorio, ejecute lo siguiente con Python 3 y JDK 17 o posterior.

```sh
python3 proyect-parser-core/tests/verificar_casos.py /tmp/pruebas-parser
```

| Caso | Java analizados | Tipos | Miembros | Relaciones | Texto | Resultado |
|---|---:|---|---|---|---|---|
""" + '\n'.join(rows) + '\n'
    if details:
        text += '\n## Diferencias de contenido\n\n' + '\n'.join(details)
    report.write_text(text, encoding='utf-8')
    print(f"{passes}/{len(cases)} casos pasan. Informe en {report}")
    return passes == len(cases)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('salida', type=Path, help='directorio para los diagramas y diffs')
    parser.add_argument('--informe', type=Path, help='ruta del informe Markdown')
    parser.add_argument('--solo-comparar', action='store_true', help='compara diagramas ya generados')
    args = parser.parse_args()
    output = args.salida.resolve()
    output.mkdir(parents=True, exist_ok=True)
    if not args.solo_comparar:
        generate(output)
    return 0 if comparison(output, args.informe or output / 'informe.md') else 1


if __name__ == '__main__':
    raise SystemExit(main())
