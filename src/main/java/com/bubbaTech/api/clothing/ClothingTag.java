package com.bubbaTech.api.clothing;

public enum ClothingTag {
    ACTIVE;

    public static ClothingTag stringToClothingTag(String value) {
        switch (value.toLowerCase()) {
            case "active":
                return ClothingTag.ACTIVE;
            default:
                return null;
        }
    }
}
