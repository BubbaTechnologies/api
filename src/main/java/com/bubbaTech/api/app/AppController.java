//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.app;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.clothing.ClothingListType;
import com.bubbaTech.api.clothing.ClothingService;
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
        ClothingDTO response = modelMapper.map(clothingService.getCard(this.getUserId(principal), typeFilter, genderFilter), ClothingDTO.class);
        response.reverseImageList();

        return EntityModel.of(response);
    }

    @RequestMapping(value = "/app/card", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsRequest() {
        return ResponseEntity.ok().build();
    }

    //Liked list for user based on sessionId
    @GetMapping(value = "/app/likes", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> likes(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        return getClothingList(this.getUserId(principal), ClothingListType.LIKE, typeFilter, genderFilter);
    }

    //Collection for user based on sessionId
    @GetMapping(value = "/app/collection", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> collection(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        return getClothingList(this.getUserId(principal), ClothingListType.BOUGHT, typeFilter, genderFilter);
    }

    //Deals with app interactions (like, image taps, dislike, remove likes)
    @PostMapping(value = "/app/like", produces = "application/json")
    public ResponseEntity<?> interaction(@RequestBody LikeDTO newLike, Principal principal){
        long userId = getUserId(principal);
        newLike.setClothing(modelMapper.map(clothingService.getById(newLike.getClothing().getId()), ClothingDTO.class));
        newLike.setUser(modelMapper.map(userService.getById(userId), UserDTO.class));

        EntityModel<LikeDTO> like = EntityModel.of(modelMapper.map(likeService.create(modelMapper.map(newLike, Like.class)),LikeDTO.class));

        return ResponseEntity.ok().body(like);
    }

    private CollectionModel<EntityModel<ClothingDTO>> getClothingList(long userId, ClothingListType listType, String typeFilter, String genderFilter) {
        List<Like> likes = likeService.getAllByUserId(userId, listType, typeFilter, genderFilter);

        List<EntityModel<ClothingDTO>> items = new ArrayList<>();

        for (Like like : likes) {
            ClothingDTO item = modelMapper.map(clothingService.getById(like.getClothing().getId()),ClothingDTO.class);
            item.reverseImageList();
            items.add(EntityModel.of(item));
        }

        return CollectionModel.of(items);
    }

    private long getUserId(Principal principal) {
        User user = userService.getByUsername(principal.getName());
        return user.getId();
    }
}
