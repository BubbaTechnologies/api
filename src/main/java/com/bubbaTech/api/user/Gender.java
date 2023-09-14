//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import java.util.Arrays;

public enum Gender {
    FEMALE(0, "Female"), MALE(1, "Male"), BOY(2, "Boy"), GIRL(3, "Girl"), KID(4, "Kid"), UNISEX(5, "Unisex");

    private final int value;
    private final String stringValue;

    Gender(int value, String stringValue) {
        this.value = value;
        this.stringValue = stringValue;
    }

    public int getIntValue() {
        return this.value;
    }
    public String getStringValue() {return this.stringValue;}

    public static String[] names() {
        return Arrays.toString(Gender.values()).replaceAll("^.|.$", "").split(", ");
    }

    static public Gender stringToGender(String gender) {
        return switch (gender.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            case "boy" -> Gender.BOY;
            case "girl" -> Gender.GIRL;
            case "kids" -> Gender.KID;
            default -> Gender.UNISEX;
        };
    }
}
