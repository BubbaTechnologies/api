//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.like;

import com.bubba.bubbaAPI.clothing.ClothingDTO;
import com.bubba.bubbaAPI.generic.DTO;
import com.bubba.bubbaAPI.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;


@Data
@JsonDeserialize(using = LikeDeserializer.class)
public class LikeDTO implements DTO<LikeDTO> {
    private Long id;

    @JsonBackReference
    private UserDTO user;
    @JsonBackReference
    private ClothingDTO clothing;
    private int rating;

    public LikeDTO() {
    }

    public LikeDTO(ClothingDTO clothing, int rating) {
        this.clothing = clothing;
        this.rating = rating;
    }

    public LikeDTO(UserDTO user, ClothingDTO clothing, int rating) {
        this.user = user;
        this.clothing = clothing;
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Like{" + "id=" + this.id + '}';
    }

}
