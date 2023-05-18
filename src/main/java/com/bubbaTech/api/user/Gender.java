//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import java.util.Arrays;

public enum Gender {
    FEMALE(0), MALE(1), BOY(2), GIRL(3), KID(4), UNISEX(5);

    private final int value;

    Gender(int value) {
        this.value = value;
    }

    public int getIntValue() {
        return this.value;
    }

    public static String[] names() {
        return Arrays.toString(Gender.values()).replaceAll("^.|.$", "").split(", ");
    }
}
