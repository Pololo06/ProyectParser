"""Ejecuta las verificaciones y genera evidencia trazable; nunca certifica XML antiguos."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import xml.etree.ElementTree as ET
import verificar_casos as corpus

ROOT = corpus.ROOT


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def source_hashes():
    files = set()
    for folder in ('proyect-parser-core/src', 'proyect-parser-core/test', 'proyect-parser-core/tests',
                   'proyect-parser-cli/src', 'proyect-parser-server/src', 'casos-generador'):
        files.update(p for p in (ROOT / folder).rglob('*')
                     if p.is_file() and p.suffix in ('.java', '.py', '.puml', '.yaml'))
    files.update(ROOT.glob('**/pom.xml'))
    return {str(p.relative_to(ROOT)).replace('\\', '/'): digest(p) for p in sorted(files)}


def validate_output(output):
    build_dirs = [ROOT / 'target', *(p.parent / 'target' for p in ROOT.glob('*/pom.xml'))]
    if any(output == directory or directory in output.parents for directory in build_dirs):
        raise ValueError('Use una salida fuera de target: Maven clean elimina esos directorios (por ejemplo out/verificacion)')


def read_tests():
    tests = []
    for folder in ('proyect-parser-core/target/surefire-reports',
                   'proyect-parser-cli/target/surefire-reports',
                   'proyect-parser-cli/target/failsafe-reports',
                   'proyect-parser-server/target/surefire-reports'):
        for path in sorted((ROOT / folder).glob('TEST-*.xml')):
            for test in ET.parse(path).getroot().findall('testcase'):
                state = 'failed' if test.find('failure') is not None or test.find('error') is not None else (
                        'skipped' if test.find('skipped') is not None else 'passed')
                tests.append({'suite': test.get('classname'), 'name': test.get('name'), 'status': state})
    return tests


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('salida', type=Path)
    parser.add_argument('--maven', default='mvn')
    parser.add_argument('--plantuml', type=Path, required=True)
    parser.add_argument('--pdf-origen', type=Path)
    parser.add_argument('--ant', type=Path, help='ejecutar integración Ant reconstruida en copias aisladas')
    args = parser.parse_args()
    output = args.salida.resolve()
    validate_output(output)
    output.mkdir(parents=True, exist_ok=True)
    manifest = {'startedUtc': datetime.now(timezone.utc).isoformat(), 'status': 'failed',
                'sources': source_hashes(), 'commands': [], 'tests': []}

    def run(name, command):
        with (output / (name + '.log')).open('w', encoding='utf-8') as log:
            result = subprocess.run([str(arg) for arg in command], cwd=ROOT, stdout=log,
                                    stderr=subprocess.STDOUT, text=True, encoding='utf-8', errors='replace')
        manifest['commands'].append({'name': name, 'argv': [str(arg) for arg in command], 'exitCode': result.returncode})
        if result.returncode:
            raise RuntimeError(f'{name}: salida {result.returncode}; véase {name}.log')

    error = None
    try:
        if args.pdf_origen:
            manifest['pdfSource'] = {'name': args.pdf_origen.name, 'sha256': digest(args.pdf_origen)}
        run('java-version', ['java', '-version'])
        # cmd launch is explicit on Windows; Maven's .cmd is not a native executable.
        maven = [args.maven, '-B', 'clean', 'verify']
        if os.name == 'nt':
            maven = [os.environ.get('COMSPEC', 'cmd.exe'), '/d', '/c', *maven]
        run('maven', maven)
        manifest['tests'] = read_tests()
        border = [t for t in manifest['tests'] if t['suite'].endswith('.BorderRequirementsTest')]
        if len(border) != 25 or any(t['status'] != 'passed' for t in border):
            raise RuntimeError('No se acreditan los 25 criterios del PDF')
        run('python', [sys.executable, '-m', 'unittest', 'discover', '-s', 'proyect-parser-core/tests', '-p', 'test_*.py'])
        manifest['fixtureCompilation'] = []
        for case in sorted(corpus.CASE_ROOT.glob('caso_[0-9][0-9]')):
            # The supplied DiagramaClases launcher targets another library, not DiagramFacade.
            sources = sorted(p for p in (case / 'src').rglob('*.java') if p.name != 'DiagramaClases.java')
            compiled = output / 'compiled' / case.name
            compiled.mkdir(parents=True, exist_ok=True)
            run('javac-' + case.name, ['javac', '--release', '17', '-encoding', 'UTF-8', '-d', compiled, *sources])
            manifest['fixtureCompilation'].append({'case': case.name, 'compiledJavaFiles': len(sources),
                                                   'excludedLauncher': 'DiagramaClases.java', 'status': 'passed'})
        run('corpus', [sys.executable, ROOT / 'proyect-parser-core/tests/verificar_casos.py', output / 'corpus'])
        run('plantuml', ['java', '-jar', args.plantuml.resolve(), '-checkonly', '-failfast2', str(output / 'corpus/*.puml')])
        if args.ant:
            import verificar_ant
            manifest['antIntegration'] = verificar_ant.verify(args.ant.resolve(), output / 'ant')
            for case in manifest['antIntegration']:
                run('plantuml-ant-' + case['case'], ['java', '-jar', args.plantuml.resolve(), '-checkonly', '-failfast2',
                    Path(case['workdir']) / 'results/diagram.puml'])
        if manifest['sources'] != source_hashes():
            raise RuntimeError('Las fuentes cambiaron durante la verificación')
        manifest['artifacts'] = {str(p.relative_to(ROOT)).replace('\\', '/'): digest(p) for p in (
            ROOT / 'proyect-parser-core/dist/proyect-parser-core-1.0.0.jar',
            ROOT / 'proyect-parser-cli/target/proyect-parser-cli-1.0.0.jar',
            ROOT / 'proyect-parser-server/target/proyect-parser-server-1.0.0.jar')}
        manifest['plantumlSha256'] = digest(args.plantuml)
        manifest['diagrams'] = {p.name: digest(p) for p in sorted((output / 'corpus').glob('caso_*.puml'))}
        manifest['status'] = 'passed-with-skip' if any(t['status'] == 'skipped' for t in manifest['tests']) else 'passed'
    except Exception as exc:
        error = str(exc)
        manifest['error'] = error
    manifest['finishedUtc'] = datetime.now(timezone.utc).isoformat()
    (output / 'evidencia.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding='utf-8')
    counts = {state: sum(t['status'] == state for t in manifest['tests']) for state in ('passed', 'failed', 'skipped')}
    report = [
        '# Informe de verificación ejecutada',
        '',
        f"Estado: **{manifest['status']}**. Ejecución UTC: {manifest['startedUtc']}.",
        '',
        f"Java: {counts['passed']} correctas, {counts['failed']} fallidas, {counts['skipped']} omitidas.",
        'Las omitidas no se consideran aprobadas. El resultado corresponde solo a las fuentes y artefactos de evidencia.json.',
        '',
        '| Comprobación | Salida | Registro |', '|---|---:|---|']
    report += [f"| {c['name']} | {c['exitCode']} | [{c['name']}.log]({c['name']}.log) |" for c in manifest['commands']]
    report += ['', '## Criterios del PDF', '', '| Prueba | Resultado |', '|---|---|']
    report += [f"| {t['name']} | {t['status']} |" for t in manifest['tests'] if t['suite'].endswith('.BorderRequirementsTest')]
    report += ['', '## Omisiones', '']
    report += [f"- {t['suite']}.{t['name']}" for t in manifest['tests'] if t['status'] == 'skipped']
    report += ['', '## Integración Ant reconstruida', '',
               'Se usan copias aisladas; se conserva el lanzador original como respaldo. '
               'No son los proyectos originales de TesterGrupos del PDF.', '', '| Caso | Estado |', '|---|---|']
    report += [f"| {case['case']} | {case['status']} |" for case in manifest.get('antIntegration', [])]
    report += ['', '## Alcance y referencia', '',
        'Contrato: BorderRequirementsTest.java (criterios de páginas 5–17 del PDF), DeclarationContractTest.java '
        '(reglas generales de declaraciones) y las demás suites Java. '
        'La comparación con los diagramas de referencia es una comprobación separada, no la definición de corrección.',
        '',
        'Se preservan en el modelo y REST: permisos de tipos sellados, expresiones de supertipos con sus argumentos, '
        'declaraciones y cotas genéricas de métodos/constructores y excepciones declaradas. '
        'Los modelos mantienen los constructores públicos existentes; los campos JSON nuevos son aditivos.',
        '',
        'Las referencias de los casos 10, 13 y 15 se actualizaron explícitamente con permits y especializaciones '
        'confirmadas en el Java. No se permiten excepciones por caso en el comparador.',
        '',
        'No es una certificación universal: no incluye carga, penetración, lógica de negocio ni resolución completa '
        'de classpath. La prueba de enlaces simbólicos sigue pendiente si aparece omitida. '
        'No se ha ejecutado la CI remota. No se reconstruyeron los 25 proyectos externos con Ant; '
        'se compilaron sus fuentes Java de modelos/aplicaciones con javac --release 17, se analizaron mediante la API pública '
        'y se validaron los diagramas nuevos con PlantUML. DiagramaClases.java se excluyó de javac porque los lanzadores '
        'del corpus local importan com.cleandev.umlgen, otra biblioteca; no se presentan como los testers externos '
        'modificados del PDF. El análisis sí lee los 112 Java y filtra esa clase del diagrama.',
        '',
        '[Fuentes, artefactos, comandos y resultados en JSON](evidencia.json).']
    if error:
        report += ['', '## Error', '', error]
    (output / 'informe.md').write_text('\n'.join(report) + '\n', encoding='utf-8')
    print(f"{manifest['status']}: {output / 'informe.md'}")
    return 1 if error else 0


if __name__ == '__main__':
    raise SystemExit(main())
