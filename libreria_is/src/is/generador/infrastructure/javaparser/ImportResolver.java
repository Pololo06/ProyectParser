package is.generador.infrastructure.javaparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves explicit file imports (paso 2 del split).
 * Maps simple names to packages so external types like
 * {@code Path} or {@code SystemModule} get their real package.
 */
class ImportResolver {

    private ImportResolver() {
    }

    /**
     * Recolecta los imports explícitos de cada archivo (simpleName -&gt; package),
     * para resolver el paquete real de tipos externos como Path o SystemModule.
     */
    static Map<String, String> extractFileImports(CompilationUnit cu) {
        Map<String, String> fileImports = new HashMap<>();
        for (ImportDeclaration id : cu.getImports()) {
            if (!id.isAsterisk() && !id.isStatic()) {
                String fqcn = id.getNameAsString();
                int lastDot = fqcn.lastIndexOf('.');
                if (lastDot > 0) {
                    String simpleName = fqcn.substring(lastDot + 1);
                    String packageName = fqcn.substring(0, lastDot);
                    fileImports.put(simpleName, packageName);
                }
            }
        }
        return fileImports;
    }
}
