"""Entrada compatible para la validación estricta del corpus, sin excepciones por caso."""

import argparse
import json
from pathlib import Path
import verificar_casos as base


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('salida', type=Path)
    parser.add_argument('--informe', type=Path,
                        default=base.ROOT / 'docs' / 'auditoria' / 'casos' / 'informe-auditado.md')
    parser.add_argument('--json', type=Path)
    parser.add_argument('--solo-comparar', action='store_true')
    args = parser.parse_args()
    output = args.salida.resolve()
    output.mkdir(parents=True, exist_ok=True)
    if not args.solo_comparar:
        base.generate(output)
    args.informe.parent.mkdir(parents=True, exist_ok=True)
    success = base.comparison(output, args.informe)
    if args.json:
        files = base.parsed_cases(output)
        cases = []
        for case in sorted(base.CASE_ROOT.glob('caso_[0-9][0-9]')):
            expected = base.extract((case / 'src' / 'clases.puml').read_text(encoding='utf-8'), True)
            actual = base.extract((output / f'{case.name}.puml').read_text(encoding='utf-8'), True)
            matches = [left == right for left, right in zip(expected, actual)]
            cases.append({'case': case.name, 'files': files[case.name],
                          'status': 'exact' if all(matches) else 'failed',
                          'typesMatch': matches[0], 'membersMatch': matches[1], 'edgesMatch': matches[2]})
        exact = sum(case['status'] == 'exact' for case in cases)
        summary = {'total': len(cases), 'exact': exact, 'validatedWithAdditions': 0,
                   'failures': len(cases) - exact,
                   'failedCases': [case['case'] for case in cases if case['status'] == 'failed'],
                   'normalizedContent': True, 'cases': cases}
        args.json.parent.mkdir(parents=True, exist_ok=True)
        args.json.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return 0 if success else 1


if __name__ == '__main__':
    raise SystemExit(main())
