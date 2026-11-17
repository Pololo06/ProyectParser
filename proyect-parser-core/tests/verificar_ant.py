"""Integración Ant reconstruida con fuentes del corpus y JAR de producción, no testers originales."""
from concurrent.futures import ThreadPoolExecutor
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import verificar_casos as base

BUILD = '''<project name="fixture" default="jar" basedir=".">
  <target name="clean"><delete dir="build"/><delete dir="dist"/></target>
  <target name="jar">
    <mkdir dir="build/classes"/><mkdir dir="dist/lib"/>
    <javac srcdir="src" destdir="build/classes" release="17" encoding="UTF-8" includeantruntime="false">
      <classpath><fileset dir="lib" includes="*.jar"/></classpath>
    </javac>
    <copy todir="dist/lib"><fileset dir="lib" includes="*.jar"/></copy>
    <jar destfile="dist/caso.jar" basedir="build/classes">
      <manifest><attribute name="Main-Class" value="org.evilmind.DiagramaClases"/>
        <attribute name="Class-Path" value="lib/core.jar lib/javaparser.jar"/></manifest>
    </jar>
  </target>
</project>'''

LAUNCHER = '''package org.evilmind;
import com.proyectparser.core.DiagramFacade;
import com.proyectparser.core.domain.policy.DiagramFilter;
import java.nio.file.*;
public class DiagramaClases {
 public static void main(String[] args) throws Exception {
  var facade = new DiagramFacade();
  var filter = new DiagramFilter.Builder().excludeClass("DiagramaClases").build();
  facade.exportPlantUml("src/org/evilmind", Path.of("results/diagram.puml"), filter);
  if (facade.getErrorCount() != 0 || facade.getParsedFileCount() == 0)
   throw new IllegalStateException(facade.getFailureReasons().toString());
  System.out.println(facade.getLastAnalysisSummary());
 }
}'''


def verify(ant, output):
    output = Path(output).resolve()
    output.mkdir(parents=True, exist_ok=True)
    parser_jar, = (base.CORE / 'lib').glob('javaparser-core-*.jar')

    def one(case):
        work = Path(tempfile.mkdtemp(prefix=case.name + '-', dir=output)).resolve()
        # Ant clean may delete only these freshly created, isolated build/dist directories.
        assert work.is_relative_to(output) and work != output
        shutil.copytree(case / 'src', work / 'src')
        launcher = work / 'src/org/evilmind/DiagramaClases.java'
        original = launcher.read_bytes()
        (work / 'original-DiagramaClases.java.txt').write_bytes(original)
        launcher.write_text(LAUNCHER, encoding='utf-8')
        (work / 'build.xml').write_text(BUILD, encoding='utf-8')
        (work / 'lib').mkdir()
        shutil.copy2(base.CORE / 'dist/proyect-parser-core-1.0.0.jar', work / 'lib/core.jar')
        shutil.copy2(parser_jar, work / 'lib/javaparser.jar')
        commands = [[str(ant), '-noinput', 'clean', 'jar'], ['java', '-jar', 'dist/caso.jar']]
        for index, command in enumerate(commands):
            if index == 0 and os.name == 'nt':
                command = [os.environ.get('COMSPEC', 'cmd.exe'), '/d', '/c', *command]
            with (work / ('ant.log' if index == 0 else 'java.log')).open('w', encoding='utf-8') as log:
                subprocess.run(command, cwd=work, check=True, stdout=log, stderr=subprocess.STDOUT)
        expected = base.extract((case / 'src/clases.puml').read_text(encoding='utf-8'), True)
        actual = base.extract((work / 'results/diagram.puml').read_text(encoding='utf-8'), True)
        if expected != actual:
            raise AssertionError(f'{case.name}: contenido diferente desde el JAR')
        return {'case': case.name, 'status': 'passed', 'workdir': str(work),
                'launcher': 'reconstructed-DiagramFacade', 'sourceLauncherBackedUp': True}

    with ThreadPoolExecutor(max_workers=4) as pool:
        results = list(pool.map(one, sorted(base.CASE_ROOT.glob('caso_[0-9][0-9]'))))
    if len(results) != 25:
        raise AssertionError('No se ejecutaron los 25 casos Ant')
    return results
