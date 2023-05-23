//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.like.LikeDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonDeserialize(using = UserDeserializer.class)
public class UserDTO implements DTO<UserDTO> {
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
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
        this.enabled = true;
        this.name = name;
    }

    public UserDTO(String username, String password, Gender gender, Boolean enabled, String name) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.likes = null;
        this.enabled = enabled;
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserDTO{" + "id=" + this.id + ", username='" + this.username + '\'' + ", password='" + this.password + '\'' + ", gender=" + this.gender + '\'' + "name='" + this.name + '\'' + "}";
    }
}
