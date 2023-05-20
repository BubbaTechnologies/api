//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

public enum ClothType {
    TOP(0), BOTTOM(1), SHOES(2), UNDERCLOTHING(3), JACKET(4), SKIRT(5), ONE_PIECE(6), ACCESSORY(7), SWIMWEAR(8), SLEEPWEAR(9), OTHER(10), DRESS(11), SET(12);

    private final int value;

    ClothType(int value) {
        this.value = value;
    }

    public int getIntValue() {
        return this.value;
    }
}
