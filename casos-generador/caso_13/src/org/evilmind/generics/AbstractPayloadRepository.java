package org.evilmind.generics;

import java.math.BigDecimal;


public abstract class AbstractPayloadRepository<T extends IdentifiablePayload> {

    protected Short repositoryPartition;

    protected AbstractPayloadRepository(Short repositoryPartition) {
        this.repositoryPartition = repositoryPartition;
    }

    public Short getRepositoryPartition() {
        return repositoryPartition;
    }


    public abstract T findById(Long id);

    public abstract Boolean persist(T entity);

    public BigDecimal extractAmount(T entity) {
        return entity != null ? entity.getDeclaredAmount() : BigDecimal.ZERO;
    }
}