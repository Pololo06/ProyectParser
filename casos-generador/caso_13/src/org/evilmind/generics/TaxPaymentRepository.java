package org.evilmind.generics;

public class TaxPaymentRepository extends AbstractPayloadRepository<TaxPaymentPayload> {

    public TaxPaymentRepository(Short repositoryPartition) {
        super(repositoryPartition);
    }

    @Override
    public TaxPaymentPayload findById(Long id) {
        return null;
    }

    @Override
    public Boolean persist(TaxPaymentPayload entity) {
        return Boolean.TRUE;
    }
}
