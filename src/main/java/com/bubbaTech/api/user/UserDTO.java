//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.like.LikeDTO;
import com.bubbaTech.api.security.authorities.Authorities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Collection;
import java.util.Date;
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
    private Date accountCreated;
    private Date lastLogin;
    @JsonIgnore
    private Collection<Authorities> grantedAuthorities;


    public UserDTO() {}

    public UserDTO(String username, String password, Gender gender) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.likes = null;
        this.enabled = true;
    }


    @Override
    public String toString() {
        return "UserDTO{" + "id=" + this.id + ", username='" + this.username + '\'' + ", password='" + this.password + '\'' + ", gender=" + this.gender + '\'' + "}";
    }
}
