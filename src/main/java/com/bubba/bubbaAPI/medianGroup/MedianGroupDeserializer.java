//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.medianGroup;

import com.bubba.bubbaAPI.clothing.ClothingDTO;
import com.bubba.bubbaAPI.user.UserDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MedianGroupDeserializer extends StdDeserializer<MedianGroupDTO> {
    public MedianGroupDeserializer() {
        this(null);
    }

    public MedianGroupDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public MedianGroupDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = jp.getCodec().readTree(jp);
        long groupID = Long.parseLong(node.get("id").textValue());
        List<String> userIDs = mapper.readValue(node.get("userIDs").textValue(), new TypeReference<>() {
        });
        List<String> clothingIDs = mapper.readValue(node.get("clothingIDs").textValue(), new TypeReference<>() {
        });

        List<UserDTO> users = new ArrayList<>();
        List<ClothingDTO> items = new ArrayList<>();

        for (String userID : userIDs) {
            long id = Long.parseLong(userID);
            users.add(new UserDTO(id));
        }

        for (String clothingID : clothingIDs) {
            long id = Long.parseLong(clothingID);
            items.add(new ClothingDTO(id));
        }

        return new MedianGroupDTO(groupID, users, items);
    }
}
