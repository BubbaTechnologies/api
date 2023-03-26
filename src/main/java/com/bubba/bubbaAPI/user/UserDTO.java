//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.user;

import com.bubba.bubbaAPI.generic.DTO;
import com.bubba.bubbaAPI.like.LikeDTO;
import com.bubba.bubbaAPI.medianGroup.MedianGroupDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonDeserialize(using = UserDeserializer.class)
public class UserDTO implements DTO<UserDTO> {
    private Long id;
    private String username;
    private String password;
    @JsonManagedReference
    private MedianGroupDTO medianGroup;
    @JsonManagedReference
    private List<LikeDTO> likes;
    private Gender gender;

    private Boolean enabled;

    private String name;

    public UserDTO() {
    }

    public UserDTO(Long id) {
        this.id = id;
    }

    public UserDTO(String username, String password, Gender gender, String name) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.likes = null;
        this.medianGroup = null;
        this.enabled = true;
        this.name = name;
    }

    public UserDTO(String username, String password, Gender gender, Boolean enabled, String name) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.likes = null;
        this.medianGroup = null;
        this.enabled = enabled;
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserDTO{" + "id=" + this.id + ", username='" + this.username + '\'' + ", password='" + this.password + '\'' + ", gender=" + this.gender + '\'' + "name='" + this.name + '\'' + "}";
    }
}
