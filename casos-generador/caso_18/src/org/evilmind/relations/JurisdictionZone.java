package org.evilmind.relations;

public class JurisdictionZone {

    private final String zoneCode;
    private final String zoneName;
    private final Short countryIsoNumeric;

    public JurisdictionZone(String zoneCode, String zoneName, Short countryIsoNumeric) {
        this.zoneCode = zoneCode;
        this.zoneName = zoneName;
        this.countryIsoNumeric = countryIsoNumeric;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getZoneName() {
        return zoneName;
    }

    public Short getCountryIsoNumeric() {
        return countryIsoNumeric;
    }
}
