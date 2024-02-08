package com.bubbaTech.api.errorLogging.clothingError;


import com.bubbaTech.api.clothing.Clothing;
import com.bubbaTech.api.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class ClothingError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Clothing clothing;

    private LocalDateTime dateTimeCreated;
}
