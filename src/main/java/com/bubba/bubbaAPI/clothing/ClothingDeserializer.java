//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.clothing;

import com.bubba.bubbaAPI.store.StoreDTO;
import com.bubba.bubbaAPI.user.Gender;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
        String imageURL = node.get("imageUrl").textValue();
        String productURL = node.get("productUrl").textValue();
        Long storeID = Long.parseLong(node.get("storeId").textValue());
        String type = node.get("type").textValue();
        JsonNode gender = node.get("gender");

        //Change type into clothType
        ClothType clothType;
        switch (type) {
            case "top":
                clothType = ClothType.TOP;
                break;
            case "bottom":
                clothType = ClothType.BOTTOM;
                break;
            case "shoes":
                clothType = ClothType.SHOES;
                break;
            case "underclothing":
                clothType = ClothType.UNDERCLOTHING;
                break;
            case "jacket":
                clothType = ClothType.JACKET;
                break;
            case "skirt":
                clothType = ClothType.SKIRT;
                break;
            case "one piece":
                clothType = ClothType.ONE_PIECE;
                break;
            case "accessory":
                clothType = ClothType.ACCESSORY;
                break;
            case "swimwear":
                clothType = ClothType.SWIMWEAR;
                break;
            case "sleepwear":
                clothType = ClothType.SLEEPWEAR;
                break;
            default:
                clothType = ClothType.OTHER;
        }

        //Change gender into Gender
        Collection<Gender> g = new ArrayList<>();
        if (gender.isArray()) {
            for (JsonNode gen : gender) {
                String ge = gen.textValue();
                switch (ge) {
                    case "male":
                        g.add(Gender.MALE);
                        break;
                    case "female":
                        g.add(Gender.FEMALE);
                        break;
                    case "boy":
                        g.add(Gender.BOY);
                        break;
                    case "girl":
                        g.add(Gender.GIRL);
                        break;
                    case "kids":
                        g.add(Gender.KID);
                        break;
                    default:
                        g.add(Gender.UNISEX);
                        break;
                }
            }
        } else {
            throw new RuntimeException();
        }

        return new ClothingDTO(name, imageURL, productURL, new StoreDTO(storeID), clothType, g);
    }
}

