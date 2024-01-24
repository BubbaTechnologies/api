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
        String email = node.get("email").textValue();
        String password = node.get("password").textValue();
        String gender = node.get("gender").textValue();
        Gender genderType = UserDeserializer.getGender(gender.toLowerCase());
        String birthdateString = node.get("birthdate").textValue();
        LocalDate birthdate = UserDeserializer.getBirthdate(birthdateString);

        return new UserDTO(username, email, password, genderType, birthdate);
    }

    public static Gender getGender(String gender) {
        return switch (gender.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            case "boy" -> Gender.BOY;
            case "girl" -> Gender.GIRL;
            default -> Gender.UNISEX;
        };
    }

    public static LocalDate getBirthdate(String birthdateString) {
        if (birthdateString == null) {
            return LocalDate.of(1,1,1);
        } else {
            String[] splitBirthdate = birthdateString.split("-");
            return LocalDate.of(Integer.parseInt(splitBirthdate[0]), Integer.parseInt(splitBirthdate[1]), Integer.parseInt(splitBirthdate[2]));
        }
    }
}
