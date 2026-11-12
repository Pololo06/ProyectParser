"""Valida el corpus completo: 20 referencias exactas y 6 adiciones justificadas.

Reutiliza el comparador y el generador de verificar_casos.py sin modificarlo.
"""

import argparse
import json
from pathlib import Path
import re
import sys

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
sys.path.insert(0, str(HERE))
sys.dont_write_bytecode = True
import verificar_casos as base  # noqa: E402

# Adiciones reales respecto a las referencias antiguas, ya explicadas en el informe.
ALLOWED_MEMBERS = {
    'caso_07': {
        ('SettlementBatchRecord', 'SettlementBatchRecord', 'constructor',
         (('batchId', 'Long'), ('batchReference', 'String'), ('totalBatchAmount', 'BigDecimal'),
          ('processingCutoff', 'OffsetDateTime'), ('priorityLevel', 'Short')), '+', (), ''),
    },
}
ALLOWED_EDGES = {
    'caso_12': {
        ('HighFrequencyRiskEngine', '', '..>', '', 'OffsetDateTime', '<<use>>'),
    },
    'caso_15': {
        ('FinancialLedgerNode', '', '..>', '', 'BigDecimal', '<<use>>'),
        ('FinancialLedgerNode', '', '..>', '', 'OffsetDateTime', '<<use>>'),
    },
    'caso_16': {
        ('TaxHoldingEntity', '', '..>', '', 'BigDecimal', '<<use>>'),
    },
    'caso_25': {
        ('FiscalInvoice', '', '..>', '', 'OffsetDateTime', '<<use>>'),
    },
}


def parsed_cases(output):
    log = (output / 'ejecucion.log').read_text(encoding='utf-8')
    parsed = dict(re.findall(r'^(caso_\d+) parsed (\d+/\d+), failed 0(?: \[\])?$', log, re.M))
    if len(parsed) != 25:
        raise ValueError(f'Se esperaban 25 casos ejecutados sin fallos, hay {len(parsed)}')
    return parsed


def row(name, files, types_ok, members_ok, edges_ok, detail):
    checks = ['Sí' if ok else 'No' for ok in (types_ok, members_ok, edges_ok)]
    return f"| {name} | {files} | {' | '.join(checks)} | {detail} |"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('salida', type=Path, help='directorio para los diagramas y diffs')
    parser.add_argument('--informe', type=Path,
                        default=ROOT / 'docs' / 'auditoria' / 'casos' / 'informe-auditado.md')
    parser.add_argument('--json', type=Path, help='escribe un resumen JSON de la validación')
    parser.add_argument('--solo-comparar', action='store_true',
                        help='no regenera: valida los .puml y ejecucion.log ya presentes en salida')
    args = parser.parse_args()
    output = args.salida.resolve()
    output.mkdir(parents=True, exist_ok=True)

    if not args.solo_comparar:
        base.generate(output)
    parsed = parsed_cases(output)
    cases = sorted(base.CASE_ROOT.glob('caso_[0-9][0-9]'))
    if len(cases) != 25:
        raise ValueError(f'Se esperaban 25 casos y se encontraron {len(cases)}')

    rows = []
    details = []
    results = []
    exact = 0
    validated = 0
    failures = 0
    for case in cases:
        name = case.name
        expected = (case / 'src' / 'clases.puml').read_text(encoding='utf-8')
        actual = (output / f'{name}.puml').read_text(encoding='utf-8')
        e_types, e_members, e_edges = base.extract(expected, True)
        a_types, a_members, a_edges = base.extract(actual, True)

        types_ok = e_types == a_types
        missing_members = e_members - a_members
        missing_edges = e_edges - a_edges
        extra_members = a_members - e_members
        extra_edges = a_edges - e_edges
        allowed_members = ALLOWED_MEMBERS.get(name, set())
        allowed_edges = ALLOWED_EDGES.get(name, set())
        missing_allowed_members = allowed_members - a_members
        missing_allowed_edges = allowed_edges - a_edges

        members_ok = (not missing_members and not missing_allowed_members
                      and extra_members == allowed_members)
        edges_ok = (not missing_edges and not missing_allowed_edges
                    and extra_edges == allowed_edges)
        ok = types_ok and members_ok and edges_ok

        allowed_count = len(allowed_members) + len(allowed_edges)
        if ok:
            if allowed_count == 0:
                exact += 1
                status = 'Exacto'
            else:
                validated += 1
                status = f'Validado (+{allowed_count})'
        else:
            failures += 1
            status = 'Fallo'
        rows.append(row(name, parsed.get(name, 'error'), types_ok, members_ok, edges_ok, status))

        case_details = []
        if not ok:
            # Contenido normalizado: tipos, miembros y relaciones, no bytes ni estilos.
            def normalized_types(types):
                return {str((type_name, kind, generics, tuple(sorted(stereotypes))))
                        for type_name, (kind, generics, stereotypes) in types.items()}

            for label, values in (
                ('Tipos esperados ausentes', normalized_types(e_types) - normalized_types(a_types)),
                ('Tipos generados adicionales', normalized_types(a_types) - normalized_types(e_types)),
                ('Miembros esperados ausentes', missing_members),
                ('Miembros permitidos ausentes', missing_allowed_members),
                ('Miembros adicionales no justificados', extra_members - allowed_members),
                ('Relaciones esperadas ausentes', missing_edges),
                ('Relaciones permitidas ausentes', missing_allowed_edges),
                ('Relaciones adicionales no justificadas', extra_edges - allowed_edges),
            ):
                for value in sorted(values):
                    case_details.append(f'- {label} · `{value}`')
            details.append(f'### {name}\n')
            details.extend(case_details)
            details.append('')

        results.append({
            'case': name,
            'files': parsed.get(name, 'error'),
            'status': 'exact' if ok and allowed_count == 0
            else 'validated' if ok else 'failed',
            'allowedAdditions': allowed_count,
            'typesMatch': types_ok,
            'membersMatch': members_ok,
            'edgesMatch': edges_ok,
            'missingAllowedMembers': sorted(str(v) for v in missing_allowed_members),
            'missingAllowedEdges': sorted(str(v) for v in missing_allowed_edges),
            'diagnostics': case_details,
        })

    total = len(cases)
    text = f"""# Informe auditado del corpus PlantUML

**{exact}/{total} exactos; {validated}/{total} validados con adiciones justificadas; {failures}/{total} fallidos.**
La igualdad es de contenido normalizado (tipos, miembros y relaciones), no de bytes ni
estilos. En caso_07, caso_12, caso_15, caso_16 y caso_25 se aceptan exclusivamente las
adiciones listadas abajo; una adición permitida ausente también invalida el caso.
Ningún caso puede tener diferencias faltantes ni adiciones no justificadas.

Generado reutilizando `verificar_casos.py` (generador y comparador originales).

| Caso | Java analizados | Tipos | Miembros | Relaciones | Resultado |
|---|---|---:|---|---|---|
""" + '\n'.join(rows) + '\n\n'

    text += """## Adiciones justificadas

- `caso_07`: constructor compacto real de `SettlementBatchRecord` con sus cinco componentes.
- `caso_12`: dependencia de `HighFrequencyRiskEngine` hacia `OffsetDateTime` por un parámetro de constructor.
- `caso_15`: dependencias de `FinancialLedgerNode` hacia `BigDecimal` y `OffsetDateTime` por parámetros de constructor.
- `caso_16`: dependencia de `TaxHoldingEntity` hacia `BigDecimal` por un parámetro de constructor.
- `caso_25`: dependencia de `FiscalInvoice` hacia `OffsetDateTime` por un parámetro de constructor.

Las referencias antiguas omitían estas capacidades ya corregidas (H06/H07); no se
modificaron los `.puml` de `casos-generador/` ni el comparador.
"""
    if details:
        text += '\n## Diferencias no aceptadas\n\n' + '\n'.join(details)
    else:
        text += '\n## Diferencias no aceptadas\n\nNinguna.\n'

    args.informe.parent.mkdir(parents=True, exist_ok=True)
    args.informe.write_text(text, encoding='utf-8')
    if args.json:
        args.json.parent.mkdir(parents=True, exist_ok=True)
        summary = {
            'total': total,
            'exact': exact,
            'validatedWithAdditions': validated,
            'failures': failures,
            'failedCases': [result['case'] for result in results if result['status'] == 'failed'],
            'normalizedContent': True,
            'cases': results,
        }
        args.json.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(f'{exact}/{total} exactos; {validated}/{total} validados con adiciones; '
          f'{failures}/{total} fallos. Informe en {args.informe}')
    return 0 if failures == 0 else 1


if __name__ == '__main__':
    raise SystemExit(main())
