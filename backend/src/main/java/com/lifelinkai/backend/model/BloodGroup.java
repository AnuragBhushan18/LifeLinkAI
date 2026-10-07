package com.lifelinkai.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BloodGroup {
    A_PLUS("A+"),
    A_MINUS("A-"),
    B_PLUS("B+"),
    B_MINUS("B-"),
    AB_PLUS("AB+"),
    AB_MINUS("AB-"),
    O_PLUS("O+"),
    O_MINUS("O-");

    private final String value;

    BloodGroup(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BloodGroup fromValue(String value) {
        for (BloodGroup bg : BloodGroup.values()) {
            if (bg.value.equalsIgnoreCase(value) || bg.name().equalsIgnoreCase(value)) {
                return bg;
            }
        }
        throw new IllegalArgumentException("Invalid BloodGroup: " + value);
    }
}
