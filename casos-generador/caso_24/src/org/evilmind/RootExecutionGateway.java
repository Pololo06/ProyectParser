package org.evilmind;

import org.evilmind.api.contracts.ExecutionRequest;
import org.evilmind.infrastructure.persistence.audit.DeepAuditStorage;

public class RootExecutionGateway {

    private final String gatewayCode;
    private final DeepAuditStorage auditStorage;

    public RootExecutionGateway(String gatewayCode, DeepAuditStorage auditStorage) {
        this.gatewayCode = gatewayCode;
        this.auditStorage = auditStorage;
    }

    public String getGatewayCode() {
        return gatewayCode;
    }

    public DeepAuditStorage getAuditStorage() {
        return auditStorage;
    }

    public Boolean dispatchTransaction(ExecutionRequest request) {
        if (request == null || request.getRequestedAmount() == null) {
            return Boolean.FALSE;
        }
        return auditStorage.persistLog(request.getRequestId(), request.getRequestedAmount());
    }
}
