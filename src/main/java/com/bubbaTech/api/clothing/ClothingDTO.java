//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.user.Gender;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Data
@JsonDeserialize(using = ClothingDeserializer.class)
@JsonSerialize(using = ClothingSerializer.class)
public class ClothingDTO implements DTO<ClothingDTO> {
    private Long id;
    private String name;
    private List<String> imageURL;
    private String productURL;
    @JsonManagedReference
    private StoreDTO store;
    private ClothType type;
    private Gender gender;
    private LocalDate date;
    private List<ClothingTag> tags;

    public ClothingDTO() {}
    public ClothingDTO(Long id) {
        this.id = id;
    }

    public ClothingDTO(String name, List<String> imageURL, String productURL, StoreDTO store, ClothType type, Gender gender, List<ClothingTag> tags) {
        this.name = name;
        this.imageURL = imageURL;
        this.productURL = productURL;
        this.store = store;
        this.type = type;
        this.gender = gender;
        this.tags = tags;
    }

    public ClothingDTO(String name, List<String> imageURL, String productURL, StoreDTO store, ClothType type, Gender gender, LocalDate date, List<ClothingTag> tags) {
        this.name = name;
        this.imageURL = imageURL;
        this.productURL = productURL;
        this.store = store;
        this.type = type;
        this.gender = gender;
        this.date = date;
        this.tags = tags;
    }

    public void reverseImageList() {
        Collections.reverse(imageURL);
    }

}
