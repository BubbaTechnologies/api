//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.like;

public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(Long id) {
        super("Could not find like " + id);
    }
}
