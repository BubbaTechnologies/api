//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.medianGroup;

public class MedianGroupNotFoundException extends RuntimeException {
    public MedianGroupNotFoundException(Long id) {
        super("Could not find median group " + id);
    }
}
