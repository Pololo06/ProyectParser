package org.evilmind.relations;

public class TaxTag {

    private final Short tagCode;
    private final String tagName;

    public TaxTag(Short tagCode, String tagName) {
        this.tagCode = tagCode;
        this.tagName = tagName;
    }

    public Short getTagCode() {
        return tagCode;
    }

    public String getTagName() {
        return tagName;
    }
}
