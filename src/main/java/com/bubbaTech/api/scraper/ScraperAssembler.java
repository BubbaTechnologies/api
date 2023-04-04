//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.scraper;

import com.bubbaTech.api.user.UserDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ScraperAssembler implements RepresentationModelAssembler<UserDTO, EntityModel<UserDTO>> {
    @Override
    public EntityModel<UserDTO> toModel(UserDTO user) {
        //TODO
        return null;
    }

}
