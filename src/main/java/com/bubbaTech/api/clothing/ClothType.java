//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

public enum ClothType {
    TOP(0, "Top"), SHIRT(1, "Shirt"), SHOES(2, "Shoes"), UNDERCLOTHING(3, "Underclothing"), JACKET_VEST(4, "Jacket & Vest"), SKIRT(5, "Skirt"), LONG_SLEEVE(6, "Long Sleeve"), ACCESSORY(7, "Accessory"), SWIMWEAR(8, "Swimwear"), SLEEPWEAR(9, "Sleepwear"), OTHER(10, "Other"), DRESS(11, "Dress"), SET(12, "Set"), SWEATSHIRT(13, "Sweatshirt"), TANK(14, "Tank")
    , BRA(15, "Bra"), SHORTS(16, "Shorts"), JEANS(17, "Jeans"), LEGGINGS(18, "Leggings"), ROMPER_JUMPER(19, "Romper & Jumper"), PANTS(20, "Pants"), SUIT_TUXEDO(21, "Suit & Tuxedo");

    private final int value;
    private final String stringValue;

    ClothType(int value, String stringValue) {
        this.value = value;
        this.stringValue = stringValue;
    }

    public int getIntValue() {
        return this.value;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public static ClothType stringToClothType(String value) {
        return switch (value.toLowerCase()) {
            case "shirt" -> ClothType.SHIRT;
            case "top" -> ClothType.TOP;
            case "long sleeve" -> ClothType.LONG_SLEEVE;
            case "long_sleeve" -> ClothType.LONG_SLEEVE;
            case "sweatshirt" -> ClothType.SWEATSHIRT;
            case "tank" -> ClothType.TANK;
            case "bra" -> ClothType.BRA;
            case "dress" -> ClothType.DRESS;
            case "jacket & vest" -> ClothType.JACKET_VEST;
            case "jacket_vest" -> ClothType.JACKET_VEST;
            case "shorts" -> ClothType.SHORTS;
            case "jeans" -> ClothType.JEANS;
            case "leggings" -> ClothType.LEGGINGS;
            case "romper & jumpsuit" -> ClothType.ROMPER_JUMPER;
            case "romper_jumpsuit" -> ClothType.ROMPER_JUMPER;
            case "skirt" -> ClothType.SKIRT;
            case "pants" -> ClothType.PANTS;
            case "set" -> ClothType.SET;
            case "sleepwear" -> ClothType.SLEEPWEAR;
            case "swimwear" -> ClothType.SWIMWEAR;
            case "shoes" -> ClothType.SHOES;
            case "suit & tuxedo" -> ClothType.SUIT_TUXEDO;
            case "suit_tuxedo" -> ClothType.SUIT_TUXEDO;
            case "underclothing" -> ClothType.UNDERCLOTHING;
            case "accessory" -> ClothType.ACCESSORY;
            default -> ClothType.OTHER;
        };
    }
}
