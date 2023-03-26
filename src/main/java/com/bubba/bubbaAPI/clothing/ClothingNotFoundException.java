//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.clothing;

public class ClothingNotFoundException extends RuntimeException {
    public ClothingNotFoundException(Long id) {
        super("Could not find item " + id);
    }
}
