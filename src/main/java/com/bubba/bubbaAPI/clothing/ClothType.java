//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.clothing;

public enum ClothType {
    TOP(0), BOTTOM(1), SHOES(3), UNDERCLOTHING(4), JACKET(6), SKIRT(7), ONE_PIECE(8), ACCESSORY(9), SWIMWEAR(10), SLEEPWEAR(11), OTHER(12);

    private final int value;

    ClothType(int value) {
        this.value = value;
    }

    public int getIntValue() {
        return this.value;
    }
}
