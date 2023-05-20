//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.user.Gender;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static com.bubbaTech.api.clothing.ClothingService.toClothType;
import static com.bubbaTech.api.clothing.ClothingService.toGender;

public class ClothingDeserializer extends StdDeserializer<ClothingDTO> {


    public ClothingDeserializer() {
        this(null);
    }

    public ClothingDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ClothingDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String name = node.get("name").textValue();
        JsonNode imageURL = node.get("imageUrl");
        String productURL = node.get("productUrl").textValue();
        long storeID = Long.parseLong(node.get("storeId").textValue());
        String type = node.get("type").textValue();
        String gender = node.get("gender").textValue();

        //Deal with imageUrl
        Collection<String> imageUrlCollection = new ArrayList<>();
        if (imageURL.isArray()){
            for (JsonNode url : imageURL)
                imageUrlCollection.add(url.textValue());
        } else {
            throw new RuntimeException();
        }

        //Change type into clothType
        ClothType clothType = toClothType(type);
        Gender g = toGender(gender);

        return new ClothingDTO(name, imageUrlCollection, productURL, new StoreDTO(storeID), clothType, g);
    }
}

