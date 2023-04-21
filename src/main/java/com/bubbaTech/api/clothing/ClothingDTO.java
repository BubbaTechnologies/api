//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.user.Gender;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Collection;

@Data
@JsonDeserialize(using = ClothingDeserializer.class)
public class ClothingDTO implements DTO<ClothingDTO> {
    private Long id;
    private String name;
    private Collection<String> imageURL;
    private String productURL;
    @JsonManagedReference
    private StoreDTO store;
    private ClothType type;
    private Collection<Gender> gender;

    public ClothingDTO() {
    }

    public ClothingDTO(Long id) {
        this.id = id;
    }

    public ClothingDTO(String name, Collection<String> imageURL, String productURL, StoreDTO store, ClothType type, Collection<Gender> gender) {
        this.name = name;
        this.imageURL = imageURL;
        this.productURL = productURL;
        this.store = store;
        this.type = type;
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Clothing{" + "id=" + this.id + ", name='" + this.name + '\'' + ", imageURL='" + this.imageURL + '\'' + ", productURL='" + this.productURL + '\'' + ", storeId=" + this.store.getId() + ", type=" + this.type + "}";
    }
}