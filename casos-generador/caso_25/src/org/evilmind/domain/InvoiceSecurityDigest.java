package org.evilmind.domain;

import java.time.OffsetDateTime;

public class InvoiceSecurityDigest {

    private final String digitalSignature;
    private final OffsetDateTime signedAt;

    public InvoiceSecurityDigest(String digitalSignature, OffsetDateTime signedAt) {
        this.digitalSignature = digitalSignature;
        this.signedAt = signedAt;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public OffsetDateTime getSignedAt() {
        return signedAt;
    }
}
