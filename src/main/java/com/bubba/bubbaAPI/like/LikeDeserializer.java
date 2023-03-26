//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.like;

import com.bubba.bubbaAPI.clothing.ClothingDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class LikeDeserializer extends StdDeserializer<LikeDTO> {
    public LikeDeserializer() {
        this(null);
    }

    public LikeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LikeDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        long clothingID = Long.parseLong(node.get("clothingId").textValue());
        int rating = Integer.parseInt(node.get("rating").textValue());
        return new LikeDTO(new ClothingDTO(clothingID), rating);
    }
}
