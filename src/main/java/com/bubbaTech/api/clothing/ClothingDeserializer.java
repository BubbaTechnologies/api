//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.user.Gender;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClothingDeserializer extends JsonDeserializer<ClothingDTO> {

    public ClothingDeserializer() {
        super();
    }

    @Override
    public ClothingDTO deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String name = node.get("name").textValue();
        JsonNode imageURL = node.get("imageUrl");
        String productURL = node.get("productUrl").textValue();
        long storeID = Long.parseLong(node.get("storeId").textValue());
        String type = node.get("type").textValue();
        String gender = node.get("gender").textValue();
        JsonNode tags = node.get("tags");

        //Deal with imageUrl
        List<String> imageUrlCollection = new ArrayList<>();
        if (imageURL.isArray()){
            for (JsonNode url : imageURL)
                imageUrlCollection.add(url.textValue());
        } else {
            throw new RuntimeException();
        }

        List<ClothingTag> tagCollection = new ArrayList<>();
        if (tags.isArray()) {
            for (JsonNode tag : tags)
                tagCollection.add(ClothingTag.stringToClothingTag(tag.textValue()));
        } else {
            throw new RuntimeException();
        }

        //Change type into clothType
        ClothType clothType = ClothType.stringToClothType(type);
        Gender g = Gender.stringToGender(gender);

        return new ClothingDTO(name, imageUrlCollection, productURL, new StoreDTO(storeID), clothType, g, tagCollection);
    }
}

