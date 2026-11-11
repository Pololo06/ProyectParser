package org.evilmind;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.evilmind.api.contracts.ExecutionRequest;
import org.evilmind.infrastructure.persistence.audit.DeepAuditStorage;

public class Case24Application {

    public static void main(String[] args) {
        DeepAuditStorage storage = new DeepAuditStorage(88L);
        RootExecutionGateway gateway = new RootExecutionGateway("GW-ROOT-01", storage);

        ExecutionRequest request = new ExecutionRequest(
                "REQ-9901-X",
                new BigDecimal("150000.00"),
                OffsetDateTime.now()
        );

        Boolean result = gateway.dispatchTransaction(request);

        System.out.println("=== Caso 24 ejecutado con exito ===");
        System.out.println("Gateway: " + gateway.getGatewayCode());
        System.out.println("Despacho Exitoso: " + result);
        System.out.println("Storage Particion: " + gateway.getAuditStorage().getStoragePartitionId());
    }
}
