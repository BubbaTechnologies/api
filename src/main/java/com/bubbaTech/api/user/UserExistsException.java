//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

public class UserExistsException extends RuntimeException {
    public UserExistsException(String str) {
        super("User exists with username/email: " + str);
    }
}
