package org.evilmind.domain;

public class TaxpayerProfile {

    private final Long taxpayerId;
    private final String legalName;
    private final Short countryCode;

    public TaxpayerProfile(Long taxpayerId, String legalName, Short countryCode) {
        this.taxpayerId = taxpayerId;
        this.legalName = legalName;
        this.countryCode = countryCode;
    }

    public Long getTaxpayerId() {
        return taxpayerId;
    }

    public String getLegalName() {
        return legalName;
    }

    public Short getCountryCode() {
        return countryCode;
    }
}
