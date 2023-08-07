//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

public enum ClothType {
    TOP(0), SHIRT(1), SHOES(2), UNDERCLOTHING(3), JACKET_VEST(4), SKIRT(5), LONG_SLEEVE(6), ACCESSORY(7), SWIMWEAR(8), SLEEPWEAR(9), OTHER(10), DRESS(11), SET(12), SWEATSHIRT(13), TANK(14)
    , BRA(15), SHORTS(16), JEANS(17), LEGGINGS(18), ROMPER_JUMPER(19), PANTS(20), SUIT_TUXEDO(21);

    private final int value;

    ClothType(int value) {
        this.value = value;
    }

    public int getIntValue() {
        return this.value;
    }

    public static ClothType stringToClothType(String value) {
        return switch (value.toLowerCase()) {
            case "shirt" -> ClothType.SHIRT;
            case "top" -> ClothType.TOP;
            case "long sleeve" -> ClothType.LONG_SLEEVE;
            case "sweatshirt" -> ClothType.SWEATSHIRT;
            case "tank" -> ClothType.TANK;
            case "bra" -> ClothType.BRA;
            case "dress" -> ClothType.DRESS;
            case "jacket & vest" -> ClothType.JACKET_VEST;
            case "shorts" -> ClothType.SHORTS;
            case "jeans" -> ClothType.JEANS;
            case "leggings" -> ClothType.LEGGINGS;
            case "romper & jumpsuit" -> ClothType.ROMPER_JUMPER;
            case "skirt" -> ClothType.SKIRT;
            case "pants" -> ClothType.PANTS;
            case "set" -> ClothType.SET;
            case "sleepwear" -> ClothType.SLEEPWEAR;
            case "swimwear" -> ClothType.SWIMWEAR;
            case "shoes" -> ClothType.SHOES;
            case "suit & tuxedo" -> ClothType.SUIT_TUXEDO;
            case "underclothing" -> ClothType.UNDERCLOTHING;
            case "accessory" -> ClothType.ACCESSORY;
            default -> ClothType.OTHER;
        };
    }
}
