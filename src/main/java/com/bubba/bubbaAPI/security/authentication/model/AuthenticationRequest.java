//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.security.authentication.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@JsonDeserialize(using = AuthenticationRequestDeserializer.class)
public class AuthenticationRequest {
    private String username;
    private String password;
}
