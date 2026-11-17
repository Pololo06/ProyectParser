import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
import verificar_entrega as delivery


class DeliveryEvidenceTest(unittest.TestCase):
    def test_results_distinguish_failures_and_skips(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            folder = root / 'proyect-parser-core/target/surefire-reports'
            folder.mkdir(parents=True)
            (folder / 'TEST-example.xml').write_text(
                '<testsuite><testcase classname="Example" name="pass"/>'
                '<testcase classname="Example" name="fail"><failure/></testcase>'
                '<testcase classname="Example" name="skip"><skipped/></testcase></testsuite>')
            with patch.object(delivery, 'ROOT', root):
                self.assertEqual(['passed', 'failed', 'skipped'], [t['status'] for t in delivery.read_tests()])

    def test_evidence_cannot_be_written_where_clean_will_delete_it(self):
        with self.assertRaises(ValueError):
            delivery.validate_output(delivery.ROOT / 'target' / 'evidence')
        delivery.validate_output(delivery.ROOT / 'out' / 'evidence')
