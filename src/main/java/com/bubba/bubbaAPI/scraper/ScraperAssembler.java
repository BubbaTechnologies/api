//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.scraper;

import com.bubba.bubbaAPI.user.UserDTO;
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
