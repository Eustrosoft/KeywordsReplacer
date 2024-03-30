package org.eustrosoft.keywords.config;

public enum KeywordType {
    COMMON("common"),
    TABLE_ROW("tableRow"),
    CHECKBOX("checkbox");

    private String value;

    KeywordType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
