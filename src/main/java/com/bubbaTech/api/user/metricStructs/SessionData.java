package com.bubbaTech.api.user.metricStructs;

import com.bubbaTech.api.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class SessionData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String sessionLength;

    private LocalDateTime dateTimeCreated;

    public SessionData() {

    }

    public SessionData(User user, String sessionLength, LocalDateTime dateTimeCreated) {
        this.user = user;
        this.sessionLength = sessionLength;
        this.dateTimeCreated = dateTimeCreated;
    }
}
