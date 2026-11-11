package org.evilmind.nested;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class EnterpriseHierarchy {

    private Long enterpriseId;
    private String enterpriseCode;
    private OffsetDateTime registeredDate;

    public EnterpriseHierarchy(Long enterpriseId, String enterpriseCode, OffsetDateTime registeredDate) {
        this.enterpriseId = enterpriseId;
        this.enterpriseCode = enterpriseCode;
        this.registeredDate = registeredDate;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public String getEnterpriseCode() {
        return enterpriseCode;
    }

    public OffsetDateTime getRegisteredDate() {
        return registeredDate;
    }

    public static class DivisionUnit {

        private Long divisionId;
        private String divisionName;
        private BigDecimal allocatedBudget;

        public DivisionUnit(Long divisionId, String divisionName, BigDecimal allocatedBudget) {
            this.divisionId = divisionId;
            this.divisionName = divisionName;
            this.allocatedBudget = allocatedBudget;
        }

        public Long getDivisionId() {
            return divisionId;
        }

        public String getDivisionName() {
            return divisionName;
        }

        public BigDecimal getAllocatedBudget() {
            return allocatedBudget;
        }

        public static class DepartmentNode {

            private Long departmentId;
            private Short costCenterCode;
            private BigDecimal operationalExpense;
            private OffsetDateTime lastAuditTime;

            public DepartmentNode(Long departmentId, Short costCenterCode, BigDecimal operationalExpense, OffsetDateTime lastAuditTime) {
                this.departmentId = departmentId;
                this.costCenterCode = costCenterCode;
                this.operationalExpense = operationalExpense;
                this.lastAuditTime = lastAuditTime;
            }

            public Long getDepartmentId() {
                return departmentId;
            }

            public Short getCostCenterCode() {
                return costCenterCode;
            }

            public BigDecimal getOperationalExpense() {
                return operationalExpense;
            }

            public OffsetDateTime getLastAuditTime() {
                return lastAuditTime;
            }
        }
    }
}
