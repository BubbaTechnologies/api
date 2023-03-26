//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.store;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(Long id) {
        super("Could not find store " + id);
    }

    public StoreNotFoundException(String s) {
        super("Could not find store " + s);
    }
}
