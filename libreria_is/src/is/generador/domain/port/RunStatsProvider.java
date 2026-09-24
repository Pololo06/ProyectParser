package is.generador.domain.port;

import java.util.List;

/**
 * Read-only traceability stats of the last analysis run (RNF-04 / RNF-06).
 * Implemented by infrastructure analyzers that track per-file results;
 * consumed by application without knowing the concrete analyzer.
 */
public interface RunStatsProvider {

    List<String> getParsedFiles();

    List<String> getFailedFiles();

    List<String> getFailureReasons();

    int getParsedFileCount();

    int getFailedFileCount();

    int getTotalJavaFileCount();
}
