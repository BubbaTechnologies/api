//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@JsonDeserialize(using = LikeDeserializer.class)
public class LikeDTO implements DTO<LikeDTO> {
    private Long id;
    @JsonBackReference
    private UserDTO user;
    @JsonBackReference
    private ClothingDTO clothing;
    @JsonIgnore
    private double rating;
    private LocalDateTime date;
    private boolean liked;
    private boolean bought;
    @JsonIgnore
    private int imageTaps;

    public LikeDTO() {}

    public LikeDTO(ClothingDTO clothing, int imageTaps) {
        this.clothing = clothing;
        this.imageTaps = imageTaps;
    }
}
