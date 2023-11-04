//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.generic.DTO;
import com.bubbaTech.api.security.authorities.Authorities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collection;

@Data
@AllArgsConstructor
@JsonDeserialize(using = UserDeserializer.class)
public class UserDTO implements DTO<UserDTO> {
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private Gender gender;
    private Boolean enabled;
    private LocalDate accountCreated;
    private LocalDate lastLogin;
    @JsonIgnore
    private Collection<Authorities> grantedAuthorities;


    public UserDTO() {}

    public UserDTO(String username, String password, Gender gender) {
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.enabled = true;
    }


    @Override
    public String toString() {
        return "UserDTO{" + "id=" + this.id + ", username='" + this.username + '\'' + ", password='" + this.password + '\'' + ", gender=" + this.gender + '\'' + "}";
    }
}
