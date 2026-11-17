package com.proyectparser.core;

import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.policy.DiagramOptions;
import com.proyectparser.core.infrastructure.plantuml.PlantUmlGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

/** Criteria transcribed from Grupo 2, pages 5–17; independent of the golden diagrams. */
class BorderRequirementsTest {
    private static void edge(ProjectModel p, String source, String type, String target) {
        assertTrue(p.getRelationships().stream().anyMatch(r -> r.getSource().equals(source)
                && r.getTarget().equals(target) && r.getType().equals(type)), source + "|" + type + "|" + target);
    }
    @ParameterizedTest(name = "PDF criterion case {0}")
    @ValueSource(ints = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25})
    void pdfRequirements(int n) throws Exception {
        var facade = new DiagramFacade();
        var p = facade.analyzeProject(Path.of("..", "casos-generador", "caso_%02d".formatted(n), "src", "org", "evilmind").toString());
        assertEquals(0, facade.getErrorCount());
        String text = new PlantUmlGenerator().render(p, new DiagramOptions.Builder().groupByPackage(false).build());
        String[] tokens = switch (n) {
            case 1 -> new String[]{"class FinancialAccount", "class LedgerEntry", "FinancialAccount +-- LedgerEntry"};
            case 2 -> new String[]{"EnterpriseHierarchy +-- DivisionUnit", "DivisionUnit +-- DepartmentNode"};
            case 3 -> new String[]{"BillingPlan +-- PaymentDiscountPolicy", "BillingPlan +-- InvoiceSnapshot", "record InvoiceSnapshot", "invoiceId : Long", "chargedAmount : BigDecimal", "issuedAt : OffsetDateTime", "taxRatePercentage : Short"};
            case 4 -> new String[]{"class LocalTaxCalculator", "processTaxesWithInternalConstructs(rawAmount : BigDecimal, multiplier : Short) : BigDecimal"};
            case 5 -> new String[]{"record FinancialAuditRecord", "auditId : Long", "trackingToken : String", "grossAmount : BigDecimal", "taxAmount : BigDecimal", "auditTimestamp : OffsetDateTime", "riskLevelCode : Short", "COMPLIANCE_VERSION", "calculateNetAmount()", "requiresManualReview()"};
            case 6 -> new String[]{"TransactionEnvelope<K, V>", "routingKey : K", "payloadData : V", "AuditablePayload <|.. TransactionEnvelope"};
            case 7 -> new String[]{"record SettlementBatchRecord", "batchId : Long", "batchReference : String", "totalBatchAmount : BigDecimal", "processingCutoff : OffsetDateTime", "priorityLevel : Short", "SettlementBatchRecord(batchId : Long, batchReference : String, totalBatchAmount : BigDecimal) : void <<Create>>"};
            case 8 -> new String[]{"enum TaxPayerType", "INDIVIDUAL : TaxPayerType", "CORPORATE : TaxPayerType", "NON_PROFIT : TaxPayerType", "hasTaxExemption() : Boolean"};
            case 9 -> new String[]{"record TaxPayerRecord", "TaxAssessmentManager +-- TaxRuleContract", "TaxAssessmentManager +-- TaxPayerRecord", "TaxAssessmentManager +-- RateStrategy"};
            case 10 -> new String[]{"<<Sealed>>", "<<Non-Sealed>>", "permits StandardFiscalLedger, CustomFiscalLedger", "+  ledgerId : Long", "#  ledgerFolio : String", "~  revisionNumber : Short", "-  accumulatedTotal : BigDecimal", "FiscalContract <|.. CustomFiscalLedger", "FiscalContract <|.. StandardFiscalLedger"};
            case 11 -> new String[]{"FinancialValuable <|-- AssetContract", "AuditableLifecycle <|-- AssetContract", "IdentifiableEntity <|-- FinancialValuable", "IdentifiableEntity <|-- AuditableLifecycle", "AssetContract <|.. CommercialRealEstate"};
            case 12 -> new String[]{"abstract class AbstractRiskEngine", "{abstract} computeRiskScore", "int[][][]", "BigDecimal[][]", "OffsetDateTime[]", "AbstractRiskEngine <|-- HighFrequencyRiskEngine"};
            case 13 -> new String[]{"AbstractPayloadRepository<T extends IdentifiablePayload>", "AbstractPayloadRepository <|-- TaxPaymentRepository : AbstractPayloadRepository<TaxPaymentPayload>", "IdentifiablePayload <|.. TaxPaymentPayload"};
            case 14 -> new String[]{"TransactionBatchSorter<T extends FinancialValuable & AuditableTransaction>", "FinancialValuable <|.. TreasuryDisbursement", "AuditableTransaction <|.. TreasuryDisbursement"};
            case 15 -> new String[]{"AbstractAuditedNode<T extends AbstractAuditedNode<T>>", "AbstractAuditedNode <|-- FinancialLedgerNode : AbstractAuditedNode<FinancialLedgerNode>", " : T"};
            case 16 -> new String[]{"Supplier<? extends FiscalEntity>", "Consumer<? super TaxHoldingEntity>", "Supplier<?>", "FiscalEntity <|-- TaxHoldingEntity"};
            case 17 -> new String[]{"List<InvoiceItem>", "Set<TaxTag>"};
            case 18 -> new String[]{"Map<JurisdictionZone,FiscalPolicy>"};
            case 19 -> new String[]{"parentCategory : TaxCategory", "subCategories : List<TaxCategory>"};
            case 20 -> new String[]{"primaryEntry : VoucherEntry", "voucherEntries : List<VoucherEntry>", "verifyEntryConsistency(externalCandidate : VoucherEntry)"};
            case 21 -> new String[]{"computeTaxLiability(taxpayer : FiscalTaxpayer)", "adjustTaxpayerIncome(originalTaxpayer : FiscalTaxpayer, adjustmentDelta : BigDecimal) : FiscalTaxpayer"};
            case 22 -> new String[]{"aggregateValues()", "aggregateValues(initialSeed : BigDecimal)", "additionalAmounts : BigDecimal[]", "tiers : TaxRateTier[]", "tierCodes : int[]"};
            case 23 -> new String[]{"{static, readOnly} DEFAULT_BASE_ISO", "{static} globalRegistryCounter", "{static, readOnly} MIN_EXCHANGE_TOLERANCE", "{static} ofBaseDefault()", "{static} ofCustomRate", "currencyIsoCode : String", "currentExchangeRate : BigDecimal"};
            case 24 -> new String[]{"dispatchTransaction(request : ExecutionRequest)", "class DeepAuditStorage", "class ExecutionRequest"};
            case 25 -> new String[]{"record InvoiceResponseDto", "invoiceNumber : String", "totalWithTaxes : BigDecimal", "issuedDate : OffsetDateTime", "AbstractAuditableEntity <|-- FiscalInvoice", "InvoiceRepository <|.. InMemoryInvoiceRepository", "InvoiceService <|.. InvoiceServiceImpl"};
            default -> throw new IllegalArgumentException();
        };
        for (String token : tokens) assertTrue(text.contains(token), "case " + n + " missing " + token);
        switch (n) {
            case 9 -> edge(p, "RateStrategy", "IMPLEMENTS", "TaxRuleContract");
            case 17 -> { edge(p, "CommercialInvoice", "AGGREGATION", "InvoiceItem"); edge(p, "CommercialInvoice", "AGGREGATION", "TaxTag"); }
            case 18 -> { edge(p, "NationalFiscalDirectory", "AGGREGATION", "JurisdictionZone"); edge(p, "NationalFiscalDirectory", "AGGREGATION", "FiscalPolicy"); }
            case 19 -> edge(p, "TaxCategory", "ASSOCIATION", "TaxCategory");
            case 20 -> {
                assertEquals(1, p.getRelationships().stream().filter(r -> r.getSource().equals("AuditVoucher") && r.getTarget().equals("VoucherEntry")).count());
            }
            case 21 -> edge(p, "TaxCalculationService", "DEPENDENCY", "FiscalTaxpayer");
            case 24 -> { edge(p, "RootExecutionGateway", "ASSOCIATION", "DeepAuditStorage"); edge(p, "RootExecutionGateway", "DEPENDENCY", "ExecutionRequest"); }
            case 25 -> {
                edge(p, "InvoiceController", "ASSOCIATION", "InvoiceService");
                edge(p, "InvoiceController", "DEPENDENCY", "InvoiceResponseDto");
                edge(p, "InvoiceServiceImpl", "ASSOCIATION", "InvoiceRepository");
                edge(p, "FiscalInvoice", "ASSOCIATION", "TaxpayerProfile");
                edge(p, "FiscalInvoice", "AGGREGATION", "InvoiceItem");
                edge(p, "FiscalInvoice", "COMPOSITION", "InvoiceSecurityDigest");
            }
        }
    }
}
