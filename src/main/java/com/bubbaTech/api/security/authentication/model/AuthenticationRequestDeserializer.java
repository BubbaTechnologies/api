//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.security.authentication.model;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class AuthenticationRequestDeserializer extends StdDeserializer<AuthenticationRequest> {

    public AuthenticationRequestDeserializer() {
        this(null);
    }

    public AuthenticationRequestDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public AuthenticationRequest deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);
        String email = node.get("email").textValue();
        String password = node.get("password").textValue();

        return new AuthenticationRequest(email, password);
    }
}
