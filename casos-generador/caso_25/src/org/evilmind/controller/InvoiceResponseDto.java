package org.evilmind.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record InvoiceResponseDto(
        String invoiceNumber,
        BigDecimal totalWithTaxes,
        OffsetDateTime issuedDate
        ) {

}
