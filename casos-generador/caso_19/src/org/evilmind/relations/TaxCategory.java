package org.evilmind.relations;

import java.math.BigDecimal;
import java.util.List;


public class TaxCategory {

    private final Long categoryId;
    private final String categoryName;
    private final BigDecimal defaultSurcharge;
    private final Short hierarchyLevel;
    private final TaxCategory parentCategory;
    private final List<TaxCategory> subCategories;

    public TaxCategory(Long categoryId, String categoryName, BigDecimal defaultSurcharge, Short hierarchyLevel, TaxCategory parentCategory, List<TaxCategory> subCategories) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.defaultSurcharge = defaultSurcharge;
        this.hierarchyLevel = hierarchyLevel;
        this.parentCategory = parentCategory;
        this.subCategories = subCategories;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getDefaultSurcharge() {
        return defaultSurcharge;
    }

    public Short getHierarchyLevel() {
        return hierarchyLevel;
    }

    public TaxCategory getParentCategory() {
        return parentCategory;
    }

    public List<TaxCategory> getSubCategories() {
        return subCategories;
    }

    public Boolean isRootCategory() {
        return parentCategory == null;
    }

    public Boolean isLeafCategory() {
        return subCategories == null || subCategories.isEmpty();
    }
}