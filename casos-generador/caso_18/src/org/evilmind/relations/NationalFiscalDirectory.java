package org.evilmind.relations;

import java.time.OffsetDateTime;
import java.util.Map;

public class NationalFiscalDirectory {

    private final Long directoryId;
    private final OffsetDateTime lastSynchronized;
    private final Map<JurisdictionZone, FiscalPolicy> zonePolicies;

    public NationalFiscalDirectory(Long directoryId, OffsetDateTime lastSynchronized, Map<JurisdictionZone, FiscalPolicy> zonePolicies) {
        this.directoryId = directoryId;
        this.lastSynchronized = lastSynchronized;
        this.zonePolicies = zonePolicies;
    }

    public Long getDirectoryId() {
        return directoryId;
    }

    public OffsetDateTime getLastSynchronized() {
        return lastSynchronized;
    }

    public Map<JurisdictionZone, FiscalPolicy> getZonePolicies() {
        return zonePolicies;
    }

    public FiscalPolicy resolvePolicy(JurisdictionZone zone) {
        return zonePolicies != null ? zonePolicies.get(zone) : null;
    }
}
