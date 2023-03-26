//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.user;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class UserDeserializer extends StdDeserializer<UserDTO> {
    public UserDeserializer() {
        this(null);
    }

    public UserDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public UserDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String username = node.get("username").textValue();
        String password = node.get("password").textValue();
        String gender = node.get("gender").textValue();
        String name = node.get("name").textValue();
        Gender g;
        switch (gender) {
            case "male":
                g = Gender.MALE;
                break;
            case "female":
                g = Gender.FEMALE;
                break;
            case "boy":
                g = Gender.BOY;
                break;
            case "girl":
                g = Gender.GIRL;
                break;
            default:
                g = Gender.UNISEX;
                break;
        }
        return new UserDTO(username, password, g, name);
    }


}
