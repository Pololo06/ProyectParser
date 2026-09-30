package com.proyectparser.core.domain.port;

import java.util.List;

/**
 * Read-only traceability stats of the last analysis run (RNF-04 / RNF-06).
 * Implemented by infrastructure analyzers that track per-file results;
 * consumed by application without knowing the concrete analyzer.
 *
 * <p>Contract: the values are those of the last {@code analyze} call that finished
 * without throwing. A failed call (e.g. a missing folder) does not reset them.
 */
public interface RunStatsProvider {

    List<String> getParsedFiles();

    List<String> getFailedFiles();

    List<String> getFailureReasons();

    int getParsedFileCount();

    int getFailedFileCount();

    int getTotalJavaFileCount();
}
