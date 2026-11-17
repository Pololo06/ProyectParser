"""Pruebas negativas del verificador: rechazar contenido incorrecto y análisis incompleto."""
import contextlib
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import verificar_casos as base


class StrictComparatorTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.cases = self.root / 'cases'
        self.output = self.root / 'output'
        self.output.mkdir()
        self.source = '  class Example {\n    + value : int\n  }\n'
        log = []
        for number in range(1, 26):
            name = f'caso_{number:02d}'
            source = self.cases / name / 'src'
            java = source / 'org' / 'evilmind' / 'Example.java'
            java.parent.mkdir(parents=True)
            java.write_text('class Example { int value; }', encoding='utf-8')
            (source / 'clases.puml').write_text(self.source, encoding='utf-8')
            (self.output / f'{name}.puml').write_text(self.source, encoding='utf-8')
            log.append(f'{name} parsed 1/1, failed 0')
        self.log = '\n'.join(log)
        (self.output / 'ejecucion.log').write_text(self.log, encoding='utf-8')

    def compare(self):
        with patch.object(base, 'CASE_ROOT', self.cases), contextlib.redirect_stdout(io.StringIO()):
            return base.comparison(self.output, self.output / 'report.md')

    def test_complete_matching_corpus_passes(self):
        self.assertTrue(self.compare())

    def test_permits_and_specialization_are_semantic_content(self):
        for declaration in ('  interface Root {\n    permits A, B\n  }\n',
                            '  class Example {\n    + <T extends Number> convert(value : T) : T throws Failure\n  }\n'):
            missing = declaration.replace('    permits A, B\n', '').replace('<T extends Number> ', '').replace(' throws Failure', '')
            self.assertNotEqual(base.extract(declaration, True), base.extract(missing, True))

    def test_missing_and_additional_content_fail(self):
        for content in ('  class Example {\n  }\n', self.source + 'Example ..> Phantom : <<use>>\n',
                        self.source.replace('+ value : int', '+ value : int\n    + extra : int')):
            with self.subTest(content=content):
                (self.output / 'caso_07.puml').write_text(content, encoding='utf-8')
                self.assertFalse(self.compare())

    def test_incomplete_duplicated_or_failed_analysis_is_rejected(self):
        for log in (self.log.replace('caso_01 parsed 1/1', 'caso_01 parsed 1/2'),
                    self.log + '\ncaso_01 parsed 1/1, failed 0',
                    self.log.replace('caso_01 parsed 1/1, failed 0', 'caso_01 parsed 0/1, failed 1')):
            with self.subTest(log=log):
                (self.output / 'ejecucion.log').write_text(log, encoding='utf-8')
                with self.assertRaises(ValueError):
                    self.compare()


if __name__ == '__main__':
    unittest.main()
