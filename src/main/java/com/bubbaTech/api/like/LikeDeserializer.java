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

import static java.lang.Math.min;

public class LikeDeserializer extends JsonDeserializer<LikeDTO> {
    public LikeDeserializer() {
        super();
    }

    @Override
    public LikeDTO deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        boolean like = node.get("like").asBoolean();
        double imageTapsRatio = node.get("imageTapRatio").asDouble();
        boolean dislike = node.get("dislike").asBoolean();
        boolean removeLike = node.get("removeLike").asBoolean();
        boolean pageClick = node.get("pageClick").asBoolean();
        boolean bought = node.get("bought").asBoolean();
        long clothingId = node.get("clothingId").asInt();

        double rating = 0;
        LikeDTO newLikeDTO = new LikeDTO(new ClothingDTO(clothingId), 0);
        if (like) {
            rating += Ratings.LIKE_RATING;
            newLikeDTO.setLiked(true);
        }

        if (dislike) {
            rating += Ratings.DISLIKE_RATING;
            newLikeDTO.setLiked(false);
        }

        if (removeLike){
            rating += Ratings.REMOVE_LIKE_RATING;
            newLikeDTO.setLiked(false);
        }

        if (pageClick)
            rating += Ratings.PAGE_CLICK_RATING;

        if (imageTapsRatio > 0) {
            rating += min(Ratings.TOTAL_IMAGE_TAP_RATING, Ratings.TOTAL_IMAGE_TAP_RATING * imageTapsRatio);
        }

        if (bought) {
            rating += Ratings.BUY_RATING;
            newLikeDTO.setBought(true);
        }
        newLikeDTO.setRating(rating);
        return newLikeDTO;
    }
}
