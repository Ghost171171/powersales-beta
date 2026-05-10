package com.model.enums;

import lombok.Getter;

@Getter
public enum ContractType {
    TYPE_1(60.0),
    TYPE_2(100.0);

    private final double value; //value of provision

    ContractType(double value) {
        this.value = value;
    }

    public static ContractType fromValue(double value) {
        for (ContractType type : ContractType.values()) {
            if (Double.compare(value, type.getValue()) == 0) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid contract type: " + value);
    }

}
