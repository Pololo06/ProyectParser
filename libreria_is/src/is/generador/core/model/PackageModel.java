package is.generador.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Package view of the project (Fase 1 / Fase 10).
 * Groups the simple type names that belong to one Java package.
 */
public class PackageModel {
    private String packageName;
    private List<String> typeNames;

    public PackageModel(String packageName, List<String> typeNames) {
        this.packageName = packageName;
        List<String> sorted = new ArrayList<>(typeNames);
        Collections.sort(sorted);
        this.typeNames = sorted;
    }

    /** Empty package is reported as "(default package)". */
    public String getDisplayName() {
        return (packageName == null || packageName.isEmpty()) ? "(default package)" : packageName;
    }

    public String getPackageName() { return packageName; }
    public List<String> getTypeNames() { return typeNames; }
    public int getTypeCount() { return typeNames.size(); }
}
