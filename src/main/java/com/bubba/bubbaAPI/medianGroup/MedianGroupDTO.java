//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.medianGroup;

import com.bubba.bubbaAPI.clothing.ClothingDTO;
import com.bubba.bubbaAPI.generic.DTO;
import com.bubba.bubbaAPI.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonDeserialize(using = MedianGroupDeserializer.class)
public class MedianGroupDTO  implements DTO<MedianGroupDTO> {
    private Long id;
    @JsonBackReference
    private List<UserDTO> users;
    @JsonBackReference
    private List<ClothingDTO> items;

    MedianGroupDTO() {
    }

    public MedianGroupDTO(Long id, List<UserDTO> users, List<ClothingDTO> items) {
        this.id = id;
        this.users = users;
        this.items = items;
    }

    @Override
    public String toString() {
        return "MedianGroup{" + "id=" + this.id + '}';
    }
}
