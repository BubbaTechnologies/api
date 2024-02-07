package com.bubbaTech.api.user.metricStructs;

import com.bubbaTech.api.user.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionDataDTO {
    private Long id;
    private UserDTO user;

    private String sessionLength;
    private LocalDateTime dateTimeCreated;

    public SessionDataDTO(String sessionLength, UserDTO user) {
        this.id = null;
        this.user = user;
        this.dateTimeCreated = LocalDateTime.now();
        this.sessionLength = sessionLength;
    }
}
