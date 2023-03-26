//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.security.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
    private final String jwt;
    private final String name;
    private final String username;
}
