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
import com.bubbaTech.api.like.Ratings;
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
import java.util.Optional;

import static java.lang.Math.min;

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

    //Deals with app like
    @PostMapping(value = "/app/like", produces = "application/json")
    public ResponseEntity<?> like(@RequestBody LikeDTO newLike, Principal principal){
        long userId = getUserId(principal);
        newLike.setClothing(modelMapper.map(clothingService.getById(newLike.getClothing().getId()), ClothingDTO.class));
        newLike.setUser(modelMapper.map(userService.getById(userId), UserDTO.class));
        //Sets like to like rating + imageTapRatio
        newLike.setRating(Ratings.LIKE_RATING + min(newLike.getImageTapsRatio() * Ratings.TOTAL_IMAGE_TAP_RATING, Ratings.TOTAL_IMAGE_TAP_RATING));
        newLike.setLiked(true);

        EntityModel<LikeDTO> like = EntityModel.of(modelMapper.map(likeService.create(modelMapper.map(newLike, Like.class)),LikeDTO.class));

        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/app/dislike", produces = "application/json")
    public ResponseEntity<?> dislike(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);
        newLike.setClothing(modelMapper.map(clothingService.getById(newLike.getClothing().getId()), ClothingDTO.class));
        newLike.setUser(modelMapper.map(userService.getById(userId), UserDTO.class));

        //Sets like to dislike rating
        newLike.setRating(Ratings.DISLIKE_RATING);
        newLike.setLiked(false);

        EntityModel<LikeDTO> like = EntityModel.of(modelMapper.map(likeService.create(modelMapper.map(newLike, Like.class)),LikeDTO.class));

        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/app/removeLike", produces = "application/json")
    public ResponseEntity<?> removeLike(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);
        newLike.setClothing(modelMapper.map(clothingService.getById(newLike.getClothing().getId()), ClothingDTO.class));
        newLike.setUser(modelMapper.map(userService.getById(userId), UserDTO.class));

        //Sets like to remove like rating
        newLike.setRating(Ratings.REMOVE_LIKE_RATING);
        newLike.setLiked(false);
        Optional<Like> findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(),newLike.getUser().getId());
        if (findLike.isPresent()) {
            newLike.setBought(findLike.get().isBought());
        } else {
            newLike.setBought(false);
        }

        EntityModel<LikeDTO> like = EntityModel.of(modelMapper.map(likeService.create(modelMapper.map(newLike, Like.class)),LikeDTO.class));

        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/app/bought", produces = "application/json")
    public ResponseEntity<?> bought(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);
        newLike.setClothing(modelMapper.map(clothingService.getById(newLike.getClothing().getId()), ClothingDTO.class));
        newLike.setUser(modelMapper.map(userService.getById(userId), UserDTO.class));

        //Sets like to like rating + imageTapRatio
        newLike.setRating(Ratings.BUY_RATING);
        Optional<Like> findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(),newLike.getUser().getId());
        if (findLike.isPresent()) {
            newLike.setLiked(findLike.get().isLiked());
        } else {
            newLike.setLiked(false);
        }
        newLike.setBought(true);

        EntityModel<LikeDTO> like = EntityModel.of(modelMapper.map(likeService.create(modelMapper.map(newLike, Like.class)),LikeDTO.class));

        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/app/pageClick", produces = "application/json")
    public ResponseEntity<?> pageClick(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);
        newLike.setClothing(modelMapper.map(clothingService.getById(newLike.getClothing().getId()), ClothingDTO.class));
        newLike.setUser(modelMapper.map(userService.getById(userId), UserDTO.class));

        newLike.setRating(Ratings.PAGE_CLICK_RATING);
        Optional<Like> findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(),newLike.getUser().getId());
        if (findLike.isPresent()) {
            newLike.setLiked(findLike.get().isLiked());
            newLike.setBought(findLike.get().isBought());
        } else {
            newLike.setLiked(false);
            newLike.setBought(false);
        }

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
