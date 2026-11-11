import com.proyectparser.core.DiagramFacade;
import com.proyectparser.core.domain.policy.DiagramFilter;
import java.nio.file.Files;
import java.nio.file.Path;

class GenerarCasos {
    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[0]);
        var filter = new DiagramFilter.Builder().excludeClass("DiagramaClases").build();
        try (var paths = Files.list(Path.of("casos-generador"))) {
            for (Path fixture : paths.filter(Files::isDirectory).sorted().toList()) {
                if (!fixture.getFileName().toString().matches("caso_[0-9]+")) {
                    continue;
                }
                var facade = new DiagramFacade();
                String uml = facade.generatePlantUml(fixture.resolve("src/org/evilmind").toString(), filter);
                if (facade.getErrorCount() != 0 || facade.getParsedFileCount() == 0) {
                    throw new IllegalStateException(fixture + " " + facade.getFailureReasons());
                }
                Files.writeString(output.resolve(fixture.getFileName() + ".puml"), uml);
                System.out.printf("%s %s%n", fixture.getFileName(), facade.getLastAnalysisSummary());
            }
        }
    }
}
