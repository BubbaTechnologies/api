//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;

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
        Gender genderType = switch (gender.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            case "boy" -> Gender.BOY;
            case "girl" -> Gender.GIRL;
            default -> Gender.UNISEX;
        };
        String birthdateString = node.get("birthdate").textValue();
        String[] splitBirthdate = birthdateString.split("-");

        LocalDate birthdate;
        if (birthdateString.equals("null")) {
            birthdate = LocalDate.of(1,1,1);
        } else {
            birthdate = LocalDate.of(Integer.parseInt(splitBirthdate[0]), Integer.parseInt(splitBirthdate[1]), Integer.parseInt(splitBirthdate[2]));
        }

        return new UserDTO(username, password, genderType, birthdate);
    }
}
