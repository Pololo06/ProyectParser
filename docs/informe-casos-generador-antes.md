# Informe de casos del generador PlantUML

**0/25 casos pasan la comparación de contenido.** Coinciden los tipos en
19/25 casos, los miembros en 2/25 y las relaciones en 0/25.
Se analizaron 112 archivos Java en las ejecuciones sin errores.
Hay 0/25 coincidencias textuales con saltos de línea normalizados.

La prueba usa `DiagramFacade.generatePlantUml` con las opciones predeterminadas y excluye
`DiagramaClases`, como hacen los proyectos de referencia. Un caso pasa si todos sus archivos
Java se analizan y coinciden sus tipos (nombre simple, categoría, genéricos y estereotipos),
sus miembros (tipos, firmas, visibilidad, modificadores mostrados e inicializadores) y sus
relaciones (extremos, flecha, multiplicidad y etiqueta). Se normalizan las dos sintaxis de
miembros, los estereotipos redundantes con la categoría, los nombres calificados de extremos,
el orden y los espacios de tipos. Los paquetes de presentación, el orden y los estilos visuales
se pueden revisar en los diffs textuales que deja el comando.

Desde la raíz del repositorio, ejecute lo siguiente con Python 3 y JDK 17 o posterior.

```sh
python3 proyect-parser-core/tests/verificar_casos.py /tmp/pruebas-parser
```

| Caso | Java analizados | Tipos | Miembros | Relaciones | Texto | Resultado |
|---|---:|---|---|---|---|---|
| caso_01 | 3/3 | Sí | Sí | No | No | Falla |
| caso_02 | 3/3 | Sí | Sí | No | No | Falla |
| caso_03 | 3/3 | Sí | No | No | No | Falla |
| caso_04 | 3/3 | Sí | No | No | No | Falla |
| caso_05 | 3/3 | Sí | No | No | No | Falla |
| caso_06 | 4/4 | No | No | No | No | Falla |
| caso_07 | 3/3 | Sí | No | No | No | Falla |
| caso_08 | 3/3 | Sí | No | No | No | Falla |
| caso_09 | 3/3 | Sí | No | No | No | Falla |
| caso_10 | 5/5 | No | No | No | No | Falla |
| caso_11 | 7/7 | No | No | No | No | Falla |
| caso_12 | 4/4 | Sí | No | No | No | Falla |
| caso_13 | 6/6 | No | No | No | No | Falla |
| caso_14 | 6/6 | No | No | No | No | Falla |
| caso_15 | 4/4 | No | No | No | No | Falla |
| caso_16 | 5/5 | Sí | No | No | No | Falla |
| caso_17 | 5/5 | Sí | No | No | No | Falla |
| caso_18 | 5/5 | Sí | No | No | No | Falla |
| caso_19 | 3/3 | Sí | No | No | No | Falla |
| caso_20 | 4/4 | Sí | No | No | No | Falla |
| caso_21 | 4/4 | Sí | No | No | No | Falla |
| caso_22 | 4/4 | Sí | No | No | No | Falla |
| caso_23 | 3/3 | Sí | No | No | No | Falla |
| caso_24 | 5/5 | Sí | No | No | No | Falla |
| caso_25 | 14/14 | Sí | No | No | No | Falla |

## Diferencias de contenido

### caso_01

- Relaciones, esperado y ausente · `('FinancialAccount', '', '+--', '', 'LedgerEntry', '')`
- Relaciones, esperado y ausente · `('FinancialAccount', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('FinancialAccount', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('LedgerEntry', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('LedgerEntry', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('FinancialAccount', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('FinancialAccount', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('FinancialAccount', '', '..>', '', 'LedgerEntry', '')`
- Relaciones, generado adicional · `('LedgerEntry', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('LedgerEntry', '', '-->', '', 'OffsetDateTime', '')`

### caso_02

- Relaciones, esperado y ausente · `('DepartmentNode', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('DepartmentNode', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('DivisionUnit', '', '+--', '', 'DepartmentNode', '')`
- Relaciones, esperado y ausente · `('DivisionUnit', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('EnterpriseHierarchy', '', '+--', '', 'DivisionUnit', '')`
- Relaciones, esperado y ausente · `('EnterpriseHierarchy', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('DepartmentNode', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('DepartmentNode', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('DivisionUnit', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('EnterpriseHierarchy', '', '-->', '', 'OffsetDateTime', '')`

### caso_03

- Miembros, esperado y ausente · `('BillingPlan', 'billingCycleDays', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('BillingPlan', 'monthlyFee', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('BillingPlan', 'planId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('BillingPlan', 'planName', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceSnapshot', 'chargedAmount', 'BigDecimal', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceSnapshot', 'invoiceId', 'Long', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceSnapshot', 'issuedAt', 'OffsetDateTime', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceSnapshot', 'taxRatePercentage', 'Short', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('PaymentDiscountPolicy', 'calculateEffectiveAmount', 'BigDecimal', (('baseAmount', 'BigDecimal'), ('discountRatePercentage', 'Short')), '+', ('abstract',), '')`
- Miembros, generado adicional · `('BillingPlan', 'billingCycleDays', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('BillingPlan', 'monthlyFee', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('BillingPlan', 'planId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('BillingPlan', 'planName', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceSnapshot', 'chargedAmount', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceSnapshot', 'invoiceId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceSnapshot', 'issuedAt', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceSnapshot', 'taxRatePercentage', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('PaymentDiscountPolicy', 'calculateEffectiveAmount', 'BigDecimal', (('baseAmount', 'BigDecimal'), ('discountRatePercentage', 'Short')), '~', (), '')`
- Relaciones, esperado y ausente · `('BillingPlan', '', '+--', '', 'InvoiceSnapshot', '')`
- Relaciones, esperado y ausente · `('BillingPlan', '', '+--', '', 'PaymentDiscountPolicy', '')`
- Relaciones, esperado y ausente · `('BillingPlan', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceSnapshot', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceSnapshot', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('PaymentDiscountPolicy', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, generado adicional · `('BillingPlan', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('InvoiceSnapshot', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('InvoiceSnapshot', '', '-->', '', 'OffsetDateTime', '')`

### caso_04

- Miembros, esperado y ausente · `('LocalTaxCalculator', 'base', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxExecutionService', 'activatedAt', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxExecutionService', 'flatRate', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxExecutionService', 'jurisdictionCode', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxExecutionService', 'serviceId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('LocalTaxCalculator', 'base', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxExecutionService', 'activatedAt', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('TaxExecutionService', 'flatRate', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxExecutionService', 'jurisdictionCode', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxExecutionService', 'serviceId', 'Long', (), '-', (), '')`
- Relaciones, esperado y ausente · `('LocalTaxCalculator', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxExecutionService', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxExecutionService', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('LocalTaxCalculator', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxExecutionService', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxExecutionService', '', '-->', '', 'OffsetDateTime', '')`

### caso_05

- Miembros, esperado y ausente · `('FinancialAuditRecord', 'COMPLIANCE_VERSION', 'String', (), '+', ('readOnly', 'static'), '"ISO-20022"')`
- Miembros, esperado y ausente · `('FinancialAuditRecord', 'auditId', 'Long', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FinancialAuditRecord', 'auditTimestamp', 'OffsetDateTime', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FinancialAuditRecord', 'grossAmount', 'BigDecimal', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FinancialAuditRecord', 'riskLevelCode', 'Short', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FinancialAuditRecord', 'taxAmount', 'BigDecimal', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FinancialAuditRecord', 'trackingToken', 'String', (), '+', ('readOnly',), '')`
- Miembros, generado adicional · `('FinancialAuditRecord', 'COMPLIANCE_VERSION', 'String', (), '+', ('static',), '')`
- Miembros, generado adicional · `('FinancialAuditRecord', 'auditId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('FinancialAuditRecord', 'auditTimestamp', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('FinancialAuditRecord', 'grossAmount', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('FinancialAuditRecord', 'riskLevelCode', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('FinancialAuditRecord', 'taxAmount', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('FinancialAuditRecord', 'trackingToken', 'String', (), '-', (), '')`
- Relaciones, esperado y ausente · `('FinancialAuditRecord', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('FinancialAuditRecord', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('FinancialAuditRecord', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('FinancialAuditRecord', '', '-->', '', 'OffsetDateTime', '')`

### caso_06

- Tipos, esperado y ausente · `('TransactionEnvelope', 'record', 'K,V>', [])`
- Tipos, generado adicional · `('TransactionEnvelope', 'record', '', [])`
- Miembros, esperado y ausente · `('AuditablePayload', 'MIN_WEIGHT_THRESHOLD', 'BigDecimal', (), '+', ('readOnly', 'static'), 'new BigDecimal("0.05")')`
- Miembros, esperado y ausente · `('AuditablePayload', 'SCHEMA_REVISION', 'Short', (), '+', ('readOnly', 'static'), '(short) 2')`
- Miembros, esperado y ausente · `('AuditablePayload', 'capturedAt', 'OffsetDateTime', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('AuditablePayload', 'evaluateWeight', 'BigDecimal', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('AuditablePayload', 'payloadVersion', 'Short', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('TransactionEnvelope', 'BASE_SURCHARGE', 'BigDecimal', (), '+', ('readOnly', 'static'), 'new BigDecimal("15.75")')`
- Miembros, esperado y ausente · `('TransactionEnvelope', 'DEFAULT_PARTITION_KEY', 'Short', (), '+', ('readOnly', 'static'), '(short) 100')`
- Miembros, esperado y ausente · `('TransactionEnvelope', 'capturedAt', 'OffsetDateTime', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TransactionEnvelope', 'commissionAmount', 'BigDecimal', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TransactionEnvelope', 'envelopeId', 'Long', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TransactionEnvelope', 'payloadData', 'V', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TransactionEnvelope', 'payloadVersion', 'Short', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TransactionEnvelope', 'routingKey', 'K', (), '+', ('readOnly',), '')`
- Miembros, generado adicional · `('AuditablePayload', 'MIN_WEIGHT_THRESHOLD', 'BigDecimal', (), '~', (), '')`
- Miembros, generado adicional · `('AuditablePayload', 'SCHEMA_REVISION', 'Short', (), '~', (), '')`
- Miembros, generado adicional · `('AuditablePayload', 'capturedAt', 'OffsetDateTime', (), '~', (), '')`
- Miembros, generado adicional · `('AuditablePayload', 'evaluateWeight', 'BigDecimal', (), '~', (), '')`
- Miembros, generado adicional · `('AuditablePayload', 'payloadVersion', 'Short', (), '~', (), '')`
- Miembros, generado adicional · `('TransactionEnvelope', 'BASE_SURCHARGE', 'BigDecimal', (), '+', ('static',), '')`
- Miembros, generado adicional · `('TransactionEnvelope', 'DEFAULT_PARTITION_KEY', 'Short', (), '+', ('static',), '')`
- Miembros, generado adicional · `('TransactionEnvelope', 'capturedAt', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('TransactionEnvelope', 'commissionAmount', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TransactionEnvelope', 'envelopeId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('TransactionEnvelope', 'payloadData', 'V', (), '-', (), '')`
- Miembros, generado adicional · `('TransactionEnvelope', 'payloadVersion', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TransactionEnvelope', 'routingKey', 'K', (), '-', (), '')`
- Relaciones, esperado y ausente · `('AuditablePayload', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('AuditablePayload', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('TransactionEnvelope', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TransactionEnvelope', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('AuditablePayload', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TransactionEnvelope', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TransactionEnvelope', '', '-->', '', 'OffsetDateTime', '')`

### caso_07

- Miembros, esperado y ausente · `('SettlementBatchRecord', 'batchId', 'Long', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('SettlementBatchRecord', 'batchReference', 'String', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('SettlementBatchRecord', 'priorityLevel', 'Short', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('SettlementBatchRecord', 'processingCutoff', 'OffsetDateTime', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('SettlementBatchRecord', 'totalBatchAmount', 'BigDecimal', (), '+', ('readOnly',), '')`
- Miembros, generado adicional · `('SettlementBatchRecord', 'batchId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('SettlementBatchRecord', 'batchReference', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('SettlementBatchRecord', 'priorityLevel', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('SettlementBatchRecord', 'processingCutoff', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('SettlementBatchRecord', 'totalBatchAmount', 'BigDecimal', (), '-', (), '')`
- Relaciones, esperado y ausente · `('SettlementBatchRecord', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('SettlementBatchRecord', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('SettlementBatchRecord', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('SettlementBatchRecord', '', '-->', '', 'OffsetDateTime', '')`

### caso_08

- Miembros, esperado y ausente · `('TaxPayerType', 'baseRetentionRate', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPayerType', 'catalogEpoch', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPayerType', 'description', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPayerType', 'typeCode', 'Short', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('TaxPayerType', 'baseRetentionRate', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPayerType', 'catalogEpoch', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPayerType', 'description', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPayerType', 'typeCode', 'Short', (), '-', (), '')`
- Relaciones, esperado y ausente · `('TaxPayerType', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxPayerType', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('TaxPayerType', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxPayerType', '', '-->', '', 'OffsetDateTime', '')`

### caso_09

- Miembros, esperado y ausente · `('RateStrategy', 'baseRate', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('RateStrategy', 'rulePriority', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxAssessmentManager', 'auditTimestamp', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxAssessmentManager', 'authorityName', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxAssessmentManager', 'jurisdictionTier', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxAssessmentManager', 'managerId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPayerRecord', 'REVENUE_LIMIT', 'BigDecimal', (), '+', ('readOnly', 'static'), 'new BigDecimal("10000000.00")')`
- Miembros, esperado y ausente · `('TaxPayerRecord', 'complianceStatus', 'Short', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPayerRecord', 'declaredAt', 'OffsetDateTime', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPayerRecord', 'declaredRevenue', 'BigDecimal', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPayerRecord', 'taxCode', 'String', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPayerRecord', 'taxpayerId', 'Long', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxRuleContract', 'evaluate', 'BigDecimal', (('payer', 'TaxPayerRecord'),), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('TaxRuleContract', 'getRulePriority', 'Short', (), '+', ('abstract',), '')`
- Miembros, generado adicional · `('RateStrategy', 'baseRate', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('RateStrategy', 'rulePriority', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TaxAssessmentManager', 'auditTimestamp', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('TaxAssessmentManager', 'authorityName', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxAssessmentManager', 'jurisdictionTier', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TaxAssessmentManager', 'managerId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPayerRecord', 'REVENUE_LIMIT', 'BigDecimal', (), '+', ('static',), '')`
- Miembros, generado adicional · `('TaxPayerRecord', 'complianceStatus', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPayerRecord', 'declaredAt', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPayerRecord', 'declaredRevenue', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPayerRecord', 'taxCode', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPayerRecord', 'taxpayerId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('TaxRuleContract', 'evaluate', 'BigDecimal', (('payer', 'TaxPayerRecord'),), '~', (), '')`
- Miembros, generado adicional · `('TaxRuleContract', 'getRulePriority', 'Short', (), '~', (), '')`
- Relaciones, esperado y ausente · `('RateStrategy', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxAssessmentManager', '', '+--', '', 'RateStrategy', '')`
- Relaciones, esperado y ausente · `('TaxAssessmentManager', '', '+--', '', 'TaxPayerRecord', '')`
- Relaciones, esperado y ausente · `('TaxAssessmentManager', '', '+--', '', 'TaxRuleContract', '')`
- Relaciones, esperado y ausente · `('TaxAssessmentManager', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxAssessmentManager', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxPayerRecord', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxPayerRecord', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxRuleContract', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxRuleContract', '', '..>', '', 'TaxPayerRecord', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxRuleContract', '', '<|..', '', 'RateStrategy', '')`
- Relaciones, generado adicional · `('RateStrategy', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxAssessmentManager', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('TaxAssessmentManager', '', '..>', '', 'RateStrategy', '')`
- Relaciones, generado adicional · `('TaxAssessmentManager', '', '..>', '', 'TaxPayerRecord', '')`
- Relaciones, generado adicional · `('TaxPayerRecord', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxPayerRecord', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('TaxRuleContract', '', '..>', '', 'TaxPayerRecord', '')`

### caso_10

- Tipos, esperado y ausente · `('CustomFiscalLedger', 'class', '', ['Non-Sealed'])`
- Tipos, esperado y ausente · `('FiscalContract', 'interface', '', ['Sealed'])`
- Tipos, esperado y ausente · `('StandardFiscalLedger', 'class', '', ['Final'])`
- Tipos, generado adicional · `('CustomFiscalLedger', 'class', '', [])`
- Tipos, generado adicional · `('FiscalContract', 'interface', '', [])`
- Tipos, generado adicional · `('StandardFiscalLedger', 'class', '', [])`
- Miembros, esperado y ausente · `('CustomFiscalLedger', 'customMultiplier', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CustomFiscalLedger', 'registeredDate', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalContract', 'DEFAULT_SURCHARGE_RATE', 'BigDecimal', (), '+', ('readOnly', 'static'), 'new BigDecimal("0.015")')`
- Miembros, esperado y ausente · `('FiscalContract', 'calculateBaseTax', 'BigDecimal', (('grossAmount', 'BigDecimal'),), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('FiscalContract', 'calculateTaxWithSurcharge', 'BigDecimal', (('grossAmount', 'BigDecimal'),), '+', ('default',), '')`
- Miembros, esperado y ausente · `('FiscalContract', 'isStandardPeriod', 'Boolean', (('timestamp', 'OffsetDateTime'),), '+', ('static',), '')`
- Miembros, generado adicional · `('CustomFiscalLedger', 'customMultiplier', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('CustomFiscalLedger', 'registeredDate', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalContract', 'DEFAULT_SURCHARGE_RATE', 'BigDecimal', (), '~', (), '')`
- Miembros, generado adicional · `('FiscalContract', 'calculateBaseTax', 'BigDecimal', (('grossAmount', 'BigDecimal'),), '~', (), '')`
- Miembros, generado adicional · `('FiscalContract', 'calculateTaxWithSurcharge', 'BigDecimal', (('grossAmount', 'BigDecimal'),), '~', (), '')`
- Miembros, generado adicional · `('FiscalContract', 'isStandardPeriod', 'Boolean', (('timestamp', 'OffsetDateTime'),), '~', ('static',), '')`
- Relaciones, esperado y ausente · `('CustomFiscalLedger', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('CustomFiscalLedger', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('FiscalContract', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('FiscalContract', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('StandardFiscalLedger', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, generado adicional · `('CustomFiscalLedger', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('CustomFiscalLedger', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('FiscalContract', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('StandardFiscalLedger', '', '-->', '', 'BigDecimal', '')`

### caso_11

- Tipos, esperado y ausente · `('CommercialRealEstate', 'class', '', ['Final'])`
- Tipos, generado adicional · `('CommercialRealEstate', 'class', '', [])`
- Miembros, esperado y ausente · `('AssetContract', 'calculateDepreciationRate', 'BigDecimal', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('AuditableLifecycle', 'getLastModifiedAt', 'OffsetDateTime', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('AuditableLifecycle', 'getLifecycleStatusCode', 'Short', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('CommercialRealEstate', 'assessedValue', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialRealEstate', 'createdAt', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialRealEstate', 'currencyCode', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialRealEstate', 'entityId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialRealEstate', 'lastModifiedAt', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialRealEstate', 'lifecycleStatusCode', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FinancialValuable', 'getAssessedValue', 'BigDecimal', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('FinancialValuable', 'getCurrencyCode', 'Short', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('IdentifiableEntity', 'getCreatedAt', 'OffsetDateTime', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('IdentifiableEntity', 'getEntityId', 'Long', (), '+', ('abstract',), '')`
- Miembros, generado adicional · `('AssetContract', 'calculateDepreciationRate', 'BigDecimal', (), '~', (), '')`
- Miembros, generado adicional · `('AuditableLifecycle', 'getLastModifiedAt', 'OffsetDateTime', (), '~', (), '')`
- Miembros, generado adicional · `('AuditableLifecycle', 'getLifecycleStatusCode', 'Short', (), '~', (), '')`
- Miembros, generado adicional · `('CommercialRealEstate', 'assessedValue', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialRealEstate', 'createdAt', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialRealEstate', 'currencyCode', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialRealEstate', 'entityId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialRealEstate', 'lastModifiedAt', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialRealEstate', 'lifecycleStatusCode', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('FinancialValuable', 'getAssessedValue', 'BigDecimal', (), '~', (), '')`
- Miembros, generado adicional · `('FinancialValuable', 'getCurrencyCode', 'Short', (), '~', (), '')`
- Miembros, generado adicional · `('IdentifiableEntity', 'getCreatedAt', 'OffsetDateTime', (), '~', (), '')`
- Miembros, generado adicional · `('IdentifiableEntity', 'getEntityId', 'Long', (), '~', (), '')`
- Relaciones, esperado y ausente · `('AssetContract', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('AuditableLifecycle', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('CommercialRealEstate', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('CommercialRealEstate', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('FinancialValuable', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('IdentifiableEntity', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('CommercialRealEstate', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('CommercialRealEstate', '', '-->', '', 'OffsetDateTime', '')`

### caso_12

- Miembros, esperado y ausente · `('AbstractRiskEngine', 'checkpointTimestamps', 'OffsetDateTime[]', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('AbstractRiskEngine', 'executeEvaluation', 'BigDecimal', (('inputCube', 'int[][][]'),), '+', ('final',), '')`
- Miembros, esperado y ausente · `('HighFrequencyRiskEngine', 'latencyThresholdMs', 'Short', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('AbstractRiskEngine', 'checkpointTimestamps', 'OffsetDateTime[]', (), '-', (), '')`
- Miembros, generado adicional · `('AbstractRiskEngine', 'executeEvaluation', 'BigDecimal', (('inputCube', 'int[][][]'),), '+', (), '')`
- Miembros, generado adicional · `('HighFrequencyRiskEngine', 'latencyThresholdMs', 'Short', (), '-', (), '')`
- Relaciones, esperado y ausente · `('AbstractRiskEngine', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('AbstractRiskEngine', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('HighFrequencyRiskEngine', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, generado adicional · `('AbstractRiskEngine', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('AbstractRiskEngine', '', '-->', '', 'OffsetDateTime', '')`

### caso_13

- Tipos, esperado y ausente · `('AbstractPayloadRepository', 'abstract class', 'TextendsIdentifiablePayload>', [])`
- Tipos, esperado y ausente · `('TaxPaymentPayload', 'class', '', ['Final'])`
- Tipos, generado adicional · `('AbstractPayloadRepository', 'abstract class', '', [])`
- Tipos, generado adicional · `('TaxPaymentPayload', 'class', '', [])`
- Miembros, esperado y ausente · `('IdentifiablePayload', 'getDeclaredAmount', 'BigDecimal', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('IdentifiablePayload', 'getEmissionDate', 'OffsetDateTime', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('IdentifiablePayload', 'getPayloadId', 'Long', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('TaxPaymentPayload', 'declaredAmount', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPaymentPayload', 'emissionDate', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxPaymentPayload', 'payloadId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('IdentifiablePayload', 'getDeclaredAmount', 'BigDecimal', (), '~', (), '')`
- Miembros, generado adicional · `('IdentifiablePayload', 'getEmissionDate', 'OffsetDateTime', (), '~', (), '')`
- Miembros, generado adicional · `('IdentifiablePayload', 'getPayloadId', 'Long', (), '~', (), '')`
- Miembros, generado adicional · `('TaxPaymentPayload', 'declaredAmount', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPaymentPayload', 'emissionDate', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('TaxPaymentPayload', 'payloadId', 'Long', (), '-', (), '')`
- Relaciones, esperado y ausente · `('AbstractPayloadRepository', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('AbstractPayloadRepository', '', '..>', '', 'IdentifiablePayload', '<<use>>')`
- Relaciones, esperado y ausente · `('IdentifiablePayload', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('IdentifiablePayload', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxPaymentPayload', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxPaymentPayload', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxPaymentRepository', '', '..>', '', 'TaxPaymentPayload', '<<use>>')`
- Relaciones, generado adicional · `('TaxPaymentPayload', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxPaymentPayload', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('TaxPaymentRepository', '', '..>', '', 'TaxPaymentPayload', '')`

### caso_14

- Tipos, esperado y ausente · `('TransactionBatchSorter', 'class', 'TextendsFinancialValuable&AuditableTransaction>', [])`
- Tipos, esperado y ausente · `('TreasuryDisbursement', 'class', '', ['Final'])`
- Tipos, generado adicional · `('TransactionBatchSorter', 'class', '', [])`
- Tipos, generado adicional · `('TreasuryDisbursement', 'class', '', [])`
- Miembros, esperado y ausente · `('AuditableTransaction', 'getExecutionTime', 'OffsetDateTime', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('AuditableTransaction', 'getTransactionSequence', 'Long', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('FinancialValuable', 'getNetAssetValue', 'BigDecimal', (), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('TransactionBatchSorter', 'batchPriority', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TreasuryDisbursement', 'executionTime', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TreasuryDisbursement', 'netAssetValue', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TreasuryDisbursement', 'transactionSequence', 'Long', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('AuditableTransaction', 'getExecutionTime', 'OffsetDateTime', (), '~', (), '')`
- Miembros, generado adicional · `('AuditableTransaction', 'getTransactionSequence', 'Long', (), '~', (), '')`
- Miembros, generado adicional · `('FinancialValuable', 'getNetAssetValue', 'BigDecimal', (), '~', (), '')`
- Miembros, generado adicional · `('TransactionBatchSorter', 'batchPriority', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TreasuryDisbursement', 'executionTime', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('TreasuryDisbursement', 'netAssetValue', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TreasuryDisbursement', 'transactionSequence', 'Long', (), '-', (), '')`
- Relaciones, esperado y ausente · `('AuditableTransaction', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('FinancialValuable', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TransactionBatchSorter', '', '..>', '', 'AuditableTransaction', '<<use>>')`
- Relaciones, esperado y ausente · `('TransactionBatchSorter', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TransactionBatchSorter', '', '..>', '', 'FinancialValuable', '<<use>>')`
- Relaciones, esperado y ausente · `('TreasuryDisbursement', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TreasuryDisbursement', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('TreasuryDisbursement', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TreasuryDisbursement', '', '-->', '', 'OffsetDateTime', '')`

### caso_15

- Tipos, esperado y ausente · `('AbstractAuditedNode', 'abstract class', 'TextendsAbstractAuditedNode<T>>', [])`
- Tipos, generado adicional · `('AbstractAuditedNode', 'abstract class', '', [])`
- Miembros, esperado y ausente · `('FinancialLedgerNode', 'ledgerLevel', 'Short', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('FinancialLedgerNode', 'ledgerLevel', 'Short', (), '-', (), '')`
- Relaciones, esperado y ausente · `('AbstractAuditedNode', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('AbstractAuditedNode', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('AbstractAuditedNode', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('AbstractAuditedNode', '', '-->', '', 'OffsetDateTime', '')`

### caso_16

- Miembros, esperado y ausente · `('FiscalEntity', 'declaredWorth', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalEntity', 'entityId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxHoldingEntity', 'holdingJurisdiction', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxHoldingEntity', 'registeredDate', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxVarianceProcessor', 'processorCode', 'Short', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('FiscalEntity', 'declaredWorth', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalEntity', 'entityId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('TaxHoldingEntity', 'holdingJurisdiction', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TaxHoldingEntity', 'registeredDate', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('TaxVarianceProcessor', 'processorCode', 'Short', (), '-', (), '')`
- Relaciones, esperado y ausente · `('FiscalEntity', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxHoldingEntity', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxVarianceProcessor', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxVarianceProcessor', '', '..>', '', 'TaxHoldingEntity', '<<use>>')`
- Relaciones, generado adicional · `('FiscalEntity', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxHoldingEntity', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('TaxVarianceProcessor', '', '..>', '', 'FiscalEntity', '')`
- Relaciones, generado adicional · `('TaxVarianceProcessor', '', '..>', '', 'TaxHoldingEntity', '')`

### caso_17

- Miembros, esperado y ausente · `('CommercialInvoice', 'applicableTags', 'Set<TaxTag>', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialInvoice', 'invoiceId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialInvoice', 'invoiceNumber', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialInvoice', 'issueDate', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('CommercialInvoice', 'lineItems', 'List<InvoiceItem>', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceItem', 'itemDescription', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceItem', 'itemId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceItem', 'quantity', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceItem', 'unitPrice', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxTag', 'tagCode', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxTag', 'tagName', 'String', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('CommercialInvoice', 'applicableTags', 'Set<TaxTag>', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialInvoice', 'invoiceId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialInvoice', 'invoiceNumber', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialInvoice', 'issueDate', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('CommercialInvoice', 'lineItems', 'List<InvoiceItem>', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceItem', 'itemDescription', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceItem', 'itemId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceItem', 'quantity', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceItem', 'unitPrice', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxTag', 'tagCode', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TaxTag', 'tagName', 'String', (), '-', (), '')`
- Relaciones, esperado y ausente · `('CommercialInvoice', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('CommercialInvoice', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('CommercialInvoice', '"1"', 'o--', '"*"', 'InvoiceItem', '')`
- Relaciones, esperado y ausente · `('CommercialInvoice', '"1"', 'o--', '"*"', 'TaxTag', '')`
- Relaciones, esperado y ausente · `('InvoiceItem', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, generado adicional · `('CommercialInvoice', '', '-->', '', 'InvoiceItem', '')`
- Relaciones, generado adicional · `('CommercialInvoice', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('CommercialInvoice', '', '-->', '', 'TaxTag', '')`
- Relaciones, generado adicional · `('InvoiceItem', '', '-->', '', 'BigDecimal', '')`

### caso_18

- Miembros, esperado y ausente · `('FiscalPolicy', 'defaultTaxRate', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalPolicy', 'effectiveFrom', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalPolicy', 'policyId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('JurisdictionZone', 'countryIsoNumeric', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('JurisdictionZone', 'zoneCode', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('JurisdictionZone', 'zoneName', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('NationalFiscalDirectory', 'directoryId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('NationalFiscalDirectory', 'lastSynchronized', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('NationalFiscalDirectory', 'zonePolicies', 'Map<JurisdictionZone,FiscalPolicy>', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('FiscalPolicy', 'defaultTaxRate', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalPolicy', 'effectiveFrom', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalPolicy', 'policyId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('JurisdictionZone', 'countryIsoNumeric', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('JurisdictionZone', 'zoneCode', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('JurisdictionZone', 'zoneName', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('NationalFiscalDirectory', 'directoryId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('NationalFiscalDirectory', 'lastSynchronized', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('NationalFiscalDirectory', 'zonePolicies', 'Map<JurisdictionZone,FiscalPolicy>', (), '-', (), '')`
- Relaciones, esperado y ausente · `('FiscalPolicy', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('FiscalPolicy', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('NationalFiscalDirectory', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('NationalFiscalDirectory', '"1"', 'o--', '"*"', 'FiscalPolicy', '')`
- Relaciones, esperado y ausente · `('NationalFiscalDirectory', '"1"', 'o--', '"*"', 'JurisdictionZone', '')`
- Relaciones, generado adicional · `('FiscalPolicy', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('FiscalPolicy', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('NationalFiscalDirectory', '', '-->', '', 'FiscalPolicy', '')`
- Relaciones, generado adicional · `('NationalFiscalDirectory', '', '-->', '', 'JurisdictionZone', '')`
- Relaciones, generado adicional · `('NationalFiscalDirectory', '', '-->', '', 'OffsetDateTime', '')`

### caso_19

- Miembros, esperado y ausente · `('TaxCategory', 'categoryId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCategory', 'categoryName', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCategory', 'defaultSurcharge', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCategory', 'hierarchyLevel', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCategory', 'parentCategory', 'TaxCategory', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCategory', 'subCategories', 'List<TaxCategory>', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('TaxCategory', 'categoryId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCategory', 'categoryName', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCategory', 'defaultSurcharge', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCategory', 'hierarchyLevel', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCategory', 'parentCategory', 'TaxCategory', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCategory', 'subCategories', 'List<TaxCategory>', (), '-', (), '')`
- Relaciones, esperado y ausente · `('TaxCategory', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxCategory', '"1"', '-->', '"1"', 'TaxCategory', '')`
- Relaciones, generado adicional · `('TaxCategory', '', '-->', '', 'BigDecimal', '')`

### caso_20

- Miembros, esperado y ausente · `('AuditVoucher', 'primaryEntry', 'VoucherEntry', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('AuditVoucher', 'totalAmount', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('AuditVoucher', 'voucherDate', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('AuditVoucher', 'voucherEntries', 'List<VoucherEntry>', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('AuditVoucher', 'voucherId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('VoucherEntry', 'accountCode', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('VoucherEntry', 'creditAmount', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('VoucherEntry', 'entryId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('VoucherEntry', 'entrySequence', 'Short', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('AuditVoucher', 'primaryEntry', 'VoucherEntry', (), '-', (), '')`
- Miembros, generado adicional · `('AuditVoucher', 'totalAmount', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('AuditVoucher', 'voucherDate', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('AuditVoucher', 'voucherEntries', 'List<VoucherEntry>', (), '-', (), '')`
- Miembros, generado adicional · `('AuditVoucher', 'voucherId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('VoucherEntry', 'accountCode', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('VoucherEntry', 'creditAmount', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('VoucherEntry', 'entryId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('VoucherEntry', 'entrySequence', 'Short', (), '-', (), '')`
- Relaciones, esperado y ausente · `('AuditVoucher', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('AuditVoucher', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('AuditVoucher', '"1"', 'o--', '"*"', 'VoucherEntry', '')`
- Relaciones, esperado y ausente · `('VoucherEntry', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, generado adicional · `('AuditVoucher', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('AuditVoucher', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('AuditVoucher', '', '-->', '', 'VoucherEntry', '')`
- Relaciones, generado adicional · `('VoucherEntry', '', '-->', '', 'BigDecimal', '')`

### caso_21

- Miembros, esperado y ausente · `('FiscalTaxpayer', 'declaredIncome', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalTaxpayer', 'registrationTimestamp', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalTaxpayer', 'taxIdentificationCode', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalTaxpayer', 'taxpayerId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCalculationService', 'serviceIdentifier', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCalculationService', 'statutoryRate', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('FiscalTaxpayer', 'declaredIncome', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalTaxpayer', 'registrationTimestamp', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalTaxpayer', 'taxIdentificationCode', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalTaxpayer', 'taxpayerId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCalculationService', 'serviceIdentifier', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCalculationService', 'statutoryRate', 'BigDecimal', (), '-', (), '')`
- Relaciones, esperado y ausente · `('FiscalTaxpayer', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('FiscalTaxpayer', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxCalculationService', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxCalculationService', '', '..>', '', 'FiscalTaxpayer', '<<use>>')`
- Relaciones, generado adicional · `('FiscalTaxpayer', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('FiscalTaxpayer', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('TaxCalculationService', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxCalculationService', '', '..>', '', 'FiscalTaxpayer', '')`

### caso_22

- Miembros, esperado y ausente · `('TaxAggregationService', 'aggregateValues', 'BigDecimal', (('initialSeed', 'BigDecimal'), ('additionalAmounts', 'BigDecimal[]')), '+', (), '')`
- Miembros, esperado y ausente · `('TaxAggregationService', 'calculateMaxTierCode', 'Short', (('tierCodes', 'int[]'),), '+', (), '')`
- Miembros, esperado y ausente · `('TaxAggregationService', 'combineTiers', 'BigDecimal', (('tiers', 'TaxRateTier[]'),), '+', (), '')`
- Miembros, esperado y ausente · `('TaxAggregationService', 'serviceName', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxRateTier', 'rateMultiplier', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxRateTier', 'tierLevel', 'Short', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('TaxAggregationService', 'aggregateValues', 'BigDecimal', (('initialSeed', 'BigDecimal'), ('additionalAmounts', 'BigDecimal')), '+', (), '')`
- Miembros, generado adicional · `('TaxAggregationService', 'calculateMaxTierCode', 'Short', (('tierCodes', 'int'),), '+', (), '')`
- Miembros, generado adicional · `('TaxAggregationService', 'combineTiers', 'BigDecimal', (('tiers', 'TaxRateTier'),), '+', (), '')`
- Miembros, generado adicional · `('TaxAggregationService', 'serviceName', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxRateTier', 'rateMultiplier', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxRateTier', 'tierLevel', 'Short', (), '-', (), '')`
- Relaciones, esperado y ausente · `('TaxAggregationService', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxAggregationService', '', '..>', '', 'TaxRateTier', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxRateTier', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, generado adicional · `('TaxAggregationService', '', '..>', '', 'TaxRateTier', '')`
- Relaciones, generado adicional · `('TaxRateTier', '', '-->', '', 'BigDecimal', '')`

### caso_23

- Miembros, esperado y ausente · `('TaxCurrencyRegistry', 'DEFAULT_BASE_ISO', 'String', (), '+', ('readOnly', 'static'), '"USD"')`
- Miembros, esperado y ausente · `('TaxCurrencyRegistry', 'MIN_EXCHANGE_TOLERANCE', 'BigDecimal', (), '-', ('readOnly', 'static'), 'new BigDecimal("0.0001")')`
- Miembros, esperado y ausente · `('TaxCurrencyRegistry', 'currencyIsoCode', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCurrencyRegistry', 'currentExchangeRate', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxCurrencyRegistry', 'globalRegistryCounter', 'Long', (), '#', ('static',), '0L')`
- Miembros, esperado y ausente · `('TaxCurrencyRegistry', 'registeredTimestamp', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('TaxCurrencyRegistry', 'DEFAULT_BASE_ISO', 'String', (), '+', ('static',), '')`
- Miembros, generado adicional · `('TaxCurrencyRegistry', 'MIN_EXCHANGE_TOLERANCE', 'BigDecimal', (), '-', ('static',), '')`
- Miembros, generado adicional · `('TaxCurrencyRegistry', 'currencyIsoCode', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCurrencyRegistry', 'currentExchangeRate', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('TaxCurrencyRegistry', 'globalRegistryCounter', 'Long', (), '#', ('static',), '')`
- Miembros, generado adicional · `('TaxCurrencyRegistry', 'registeredTimestamp', 'OffsetDateTime', (), '-', (), '')`
- Relaciones, esperado y ausente · `('TaxCurrencyRegistry', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('TaxCurrencyRegistry', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, generado adicional · `('TaxCurrencyRegistry', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('TaxCurrencyRegistry', '', '-->', '', 'OffsetDateTime', '')`

### caso_24

- Miembros, esperado y ausente · `('DeepAuditStorage', 'storagePartitionId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('ExecutionRequest', 'requestId', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('ExecutionRequest', 'requestTime', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('ExecutionRequest', 'requestedAmount', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('RootExecutionGateway', 'auditStorage', 'DeepAuditStorage', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('RootExecutionGateway', 'gatewayCode', 'String', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('DeepAuditStorage', 'storagePartitionId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('ExecutionRequest', 'requestId', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('ExecutionRequest', 'requestTime', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('ExecutionRequest', 'requestedAmount', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('RootExecutionGateway', 'auditStorage', 'DeepAuditStorage', (), '-', (), '')`
- Miembros, generado adicional · `('RootExecutionGateway', 'gatewayCode', 'String', (), '-', (), '')`
- Relaciones, esperado y ausente · `('DeepAuditStorage', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('DeepAuditStorage', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('ExecutionRequest', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('ExecutionRequest', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('RootExecutionGateway', '', '..>', '', 'ExecutionRequest', '<<use>>')`
- Relaciones, esperado y ausente · `('RootExecutionGateway', '"1"', '-->', '"1"', 'DeepAuditStorage', '')`
- Relaciones, generado adicional · `('DeepAuditStorage', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('ExecutionRequest', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('ExecutionRequest', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('RootExecutionGateway', '', '-->', '', 'DeepAuditStorage', '')`
- Relaciones, generado adicional · `('RootExecutionGateway', '', '..>', '', 'ExecutionRequest', '')`

### caso_25

- Miembros, esperado y ausente · `('FiscalInvoice', 'invoiceNumber', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalInvoice', 'issuer', 'TaxpayerProfile', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalInvoice', 'items', 'List<InvoiceItem>', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalInvoice', 'netTotal', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('FiscalInvoice', 'securityDigest', 'InvoiceSecurityDigest', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InMemoryInvoiceRepository', 'operationCounter', 'Long', (), '~', (), '0L')`
- Miembros, esperado y ausente · `('InvoiceController', 'invoiceService', 'InvoiceService', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceItem', 'description', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceItem', 'itemId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceItem', 'quantity', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceItem', 'unitPrice', 'BigDecimal', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceRepository', 'findById', 'FiscalInvoice', (('id', 'Long'),), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('InvoiceRepository', 'save', 'Boolean', (('invoice', 'FiscalInvoice'),), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('InvoiceResponseDto', 'invoiceNumber', 'String', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceResponseDto', 'issuedDate', 'OffsetDateTime', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceResponseDto', 'totalWithTaxes', 'BigDecimal', (), '+', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceSecurityDigest', 'digitalSignature', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceSecurityDigest', 'signedAt', 'OffsetDateTime', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('InvoiceService', 'computeTaxes', 'BigDecimal', (('invoice', 'FiscalInvoice'),), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('InvoiceService', 'registerInvoice', 'Boolean', (('invoice', 'FiscalInvoice'),), '+', ('abstract',), '')`
- Miembros, esperado y ausente · `('InvoiceServiceImpl', 'invoiceRepository', 'InvoiceRepository', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxpayerProfile', 'countryCode', 'Short', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxpayerProfile', 'legalName', 'String', (), '-', ('readOnly',), '')`
- Miembros, esperado y ausente · `('TaxpayerProfile', 'taxpayerId', 'Long', (), '-', ('readOnly',), '')`
- Miembros, generado adicional · `('FiscalInvoice', 'invoiceNumber', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalInvoice', 'issuer', 'TaxpayerProfile', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalInvoice', 'items', 'List<InvoiceItem>', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalInvoice', 'netTotal', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('FiscalInvoice', 'securityDigest', 'InvoiceSecurityDigest', (), '-', (), '')`
- Miembros, generado adicional · `('InMemoryInvoiceRepository', 'operationCounter', 'Long', (), '~', (), '')`
- Miembros, generado adicional · `('InvoiceController', 'invoiceService', 'InvoiceService', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceItem', 'description', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceItem', 'itemId', 'Long', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceItem', 'quantity', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceItem', 'unitPrice', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceRepository', 'findById', 'FiscalInvoice', (('id', 'Long'),), '~', (), '')`
- Miembros, generado adicional · `('InvoiceRepository', 'save', 'Boolean', (('invoice', 'FiscalInvoice'),), '~', (), '')`
- Miembros, generado adicional · `('InvoiceResponseDto', 'invoiceNumber', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceResponseDto', 'issuedDate', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceResponseDto', 'totalWithTaxes', 'BigDecimal', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceSecurityDigest', 'digitalSignature', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceSecurityDigest', 'signedAt', 'OffsetDateTime', (), '-', (), '')`
- Miembros, generado adicional · `('InvoiceService', 'computeTaxes', 'BigDecimal', (('invoice', 'FiscalInvoice'),), '~', (), '')`
- Miembros, generado adicional · `('InvoiceService', 'registerInvoice', 'Boolean', (('invoice', 'FiscalInvoice'),), '~', (), '')`
- Miembros, generado adicional · `('InvoiceServiceImpl', 'invoiceRepository', 'InvoiceRepository', (), '-', (), '')`
- Miembros, generado adicional · `('TaxpayerProfile', 'countryCode', 'Short', (), '-', (), '')`
- Miembros, generado adicional · `('TaxpayerProfile', 'legalName', 'String', (), '-', (), '')`
- Miembros, generado adicional · `('TaxpayerProfile', 'taxpayerId', 'Long', (), '-', (), '')`
- Relaciones, esperado y ausente · `('AbstractAuditableEntity', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('FiscalInvoice', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('FiscalInvoice', '"1"', '*--', '"1"', 'InvoiceSecurityDigest', '')`
- Relaciones, esperado y ausente · `('FiscalInvoice', '"1"', '-->', '"1"', 'TaxpayerProfile', '')`
- Relaciones, esperado y ausente · `('FiscalInvoice', '"1"', 'o--', '"*"', 'InvoiceItem', '')`
- Relaciones, esperado y ausente · `('InMemoryInvoiceRepository', '', '..>', '', 'FiscalInvoice', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceController', '', '..>', '', 'FiscalInvoice', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceController', '', '..>', '', 'InvoiceResponseDto', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceController', '"1"', '-->', '"1"', 'InvoiceService', '')`
- Relaciones, esperado y ausente · `('InvoiceItem', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceRepository', '', '..>', '', 'FiscalInvoice', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceResponseDto', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceResponseDto', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceSecurityDigest', '', '..>', '', 'OffsetDateTime', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceService', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceService', '', '..>', '', 'FiscalInvoice', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceServiceImpl', '', '..>', '', 'BigDecimal', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceServiceImpl', '', '..>', '', 'FiscalInvoice', '<<use>>')`
- Relaciones, esperado y ausente · `('InvoiceServiceImpl', '"1"', '-->', '"1"', 'InvoiceRepository', '')`
- Relaciones, generado adicional · `('AbstractAuditableEntity', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('FiscalInvoice', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('FiscalInvoice', '', '-->', '', 'InvoiceItem', '')`
- Relaciones, generado adicional · `('FiscalInvoice', '', '-->', '', 'InvoiceSecurityDigest', '')`
- Relaciones, generado adicional · `('FiscalInvoice', '', '-->', '', 'TaxpayerProfile', '')`
- Relaciones, generado adicional · `('InMemoryInvoiceRepository', '', '..>', '', 'FiscalInvoice', '')`
- Relaciones, generado adicional · `('InvoiceController', '', '-->', '', 'InvoiceService', '')`
- Relaciones, generado adicional · `('InvoiceController', '', '..>', '', 'FiscalInvoice', '')`
- Relaciones, generado adicional · `('InvoiceController', '', '..>', '', 'InvoiceResponseDto', '')`
- Relaciones, generado adicional · `('InvoiceItem', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('InvoiceRepository', '', '..>', '', 'FiscalInvoice', '')`
- Relaciones, generado adicional · `('InvoiceResponseDto', '', '-->', '', 'BigDecimal', '')`
- Relaciones, generado adicional · `('InvoiceResponseDto', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('InvoiceSecurityDigest', '', '-->', '', 'OffsetDateTime', '')`
- Relaciones, generado adicional · `('InvoiceService', '', '..>', '', 'FiscalInvoice', '')`
- Relaciones, generado adicional · `('InvoiceServiceImpl', '', '-->', '', 'InvoiceRepository', '')`
- Relaciones, generado adicional · `('InvoiceServiceImpl', '', '..>', '', 'FiscalInvoice', '')`
