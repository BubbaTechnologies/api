package com.bubbaTech.api.clothing;

public enum ClothingTag {
    ACTIVE(0, "Active");

    private final int intValue;
    private final String stringValue;

    ClothingTag(int intValue, String stringValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public static ClothingTag stringToClothingTag(String value) {
        switch (value.toLowerCase()) {
            case "active":
                return ClothingTag.ACTIVE;
            default:
                return null;
        }
    }
}
