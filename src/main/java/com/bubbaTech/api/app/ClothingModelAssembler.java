//package com.bubba.bubbaAPI.modelAssemblers;
//
//import clothing.com.bubbaTech.api.ClothingDTO;
//import com.bubba.bubbaAPI.controllers.ClothingController;
//import org.springframework.hateoas.EntityModel;
//import org.springframework.hateoas.server.RepresentationModelAssembler;
//import org.springframework.stereotype.Component;
//
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
//
//@Component
//public
//class ClothingModelAssembler implements RepresentationModelAssembler<ClothingDTO, EntityModel<ClothingDTO>> {
//    @Override
//    public EntityModel<ClothingDTO> toModel(ClothingDTO clothing) {
//        return EntityModel.of(clothing);
//    }
//
//    public EntityModel<ClothingDTO> toModel(ClothingDTO clothing, long sessionId) {
//        EntityModel<ClothingDTO> model = EntityModel.of(clothing,
//                linkTo(methodOn(ClothingController.class).getCard(sessionId)).withRel("card"),
//                linkTo(methodOn(ClothingController.class).getRecommendationList(sessionId)).withRel("recommendationList"),
//                linkTo(methodOn(ClothingController.class).getLikesByRating(sessionId)).withRel("likedList")
//        );
//
//        return model;
//    }
//}