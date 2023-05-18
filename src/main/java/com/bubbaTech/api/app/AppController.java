//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.app;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.clothing.ClothingService;
import com.bubbaTech.api.info.FilterOptionsDTO;
import com.bubbaTech.api.like.Like;
import com.bubbaTech.api.like.LikeDTO;
import com.bubbaTech.api.like.LikeService;
import com.bubbaTech.api.user.User;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@AllArgsConstructor
public class AppController {
    UserService userService;
    ClothingService clothingService;
    ModelMapper modelMapper;
    LikeService likeService;

    //Clothing card for user based on sessionId

    @GetMapping(value = "/app/card", produces = "application/json")
    public EntityModel<ClothingDTO> card(
            Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        ClothingDTO response;
        //TODO: Get any type but other
        response = modelMapper.map(clothingService.getCard(this.getUserId(principal), typeFilter, genderFilter), ClothingDTO.class);

        return EntityModel.of(response,
                linkTo(methodOn(AppController.class).card(principal, null, null)).withSelfRel(),
                linkTo(methodOn(AppController.class).createLike(new LikeDTO(response, 5), principal)).withRel("createLike"),
                linkTo(methodOn(AppController.class).createLike(new LikeDTO(response, 10), principal)).withRel("createLove"));
    }

    @RequestMapping(value = "/app/card", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsRequest() {
        return ResponseEntity.ok().build();
    }

    //Liked list for user based on sessionId
    @GetMapping(value = "/app/likes", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> likes(Principal principal) {
        return getClothingList(this.getUserId(principal), 5);
    }

    //Collection for user based on sessionId
    @GetMapping(value = "/app/collection", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> collection(Principal principal) {
        return getClothingList(this.getUserId(principal), 10);
    }


    //Create like
    @PostMapping(value = "/app/like", produces = "application/json")
    public ResponseEntity<?> createLike(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);

        newLike.setClothing(modelMapper.map(clothingService.getById(newLike.getClothing().getId()),ClothingDTO.class));
        newLike.setUser(modelMapper.map(userService.getById(userId), UserDTO.class));

        EntityModel<LikeDTO> like = EntityModel.of(modelMapper.map(likeService.create(modelMapper.map(newLike, Like.class)),LikeDTO.class));

        return ResponseEntity.ok().body(like);
    }

    //Update like
    @PutMapping(value = "/app/like", produces = "application/json")
    public ResponseEntity<?> updateLike(@RequestBody LikeDTO likeRequest, Principal principal) {
        likeRequest.setClothing(modelMapper.map(clothingService.getById(likeRequest.getClothing().getId()), ClothingDTO.class));
        long userId = getUserId(principal);
        likeRequest.setUser(modelMapper.map(userService.getById(userId), UserDTO.class));
        likeRequest = modelMapper.map(likeService.update(modelMapper.map(likeRequest, Like.class)),LikeDTO.class);
        EntityModel<LikeDTO> entityModel = EntityModel.of(likeRequest);

        return ResponseEntity.ok().body(entityModel);
    }

    @GetMapping(value = "/app/filterOptions", produces = "application/json")
    public ResponseEntity<?> filterOptions() {
        return ResponseEntity.ok().body(EntityModel.of(new FilterOptionsDTO()));
    }

    private CollectionModel<EntityModel<ClothingDTO>> getClothingList(long userId, int rating) {
        List<Like> likes = likeService.getAllByUserId(userId, rating);
        List<EntityModel<ClothingDTO>> items = new ArrayList<>();

        for (Like like : likes)
            items.add(EntityModel.of(modelMapper.map(clothingService.getById(like.getClothing().getId()),ClothingDTO.class)));

        return CollectionModel.of(items);
    }

    private long getUserId(Principal principal) {
        User user = userService.getByUsername(principal.getName());
        return user.getId();
    }
}
