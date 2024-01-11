package com.bubbaTech.api.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileDTO {
    private Long id;
    private String username;
    private Boolean privateAccount;

    public ProfileDTO(UserDTO userDTO) {
        this.id = userDTO.getId();
        this.username = userDTO.getUsername();
        this.privateAccount = userDTO.getPrivateAccount();
    }
}