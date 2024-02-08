package com.bubbaTech.api.errorLogging.clothingError;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.user.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClothingErrorDTO {
    private Long id;
    private UserDTO user;
    private ClothingDTO clothing;
    private LocalDateTime dateTimeCreated;

    public ClothingErrorDTO() {}

    public ClothingErrorDTO(UserDTO user, ClothingDTO clothing) {
        this.id = null;
        this.user = user;
        this.clothing = clothing;
        this.dateTimeCreated = LocalDateTime.now();
    }
}
