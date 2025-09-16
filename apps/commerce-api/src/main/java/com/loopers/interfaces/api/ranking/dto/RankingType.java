package com.loopers.interfaces.api.ranking.dto;

public enum RankingType {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");
    
    private final String value;
    
    RankingType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static RankingType fromValue(String value) {
        for (RankingType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ranking type: " + value);
    }
}
