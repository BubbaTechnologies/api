//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class LikeDeserializer extends JsonDeserializer<LikeDTO> {
    public LikeDeserializer() {
        super();
    }

    @Override
    public LikeDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        int imageTaps = node.get("imageTaps").asInt();
        long clothingId = node.get("clothingId").asInt();
        return new LikeDTO(new ClothingDTO(clothingId), imageTaps);
    }
}
