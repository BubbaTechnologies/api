//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.app;

import com.bubbaTech.api.clothing.Clothing;
import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.clothing.ClothingListType;
import com.bubbaTech.api.clothing.ClothingService;
import com.bubbaTech.api.like.Like;
import com.bubbaTech.api.like.LikeDTO;
import com.bubbaTech.api.like.LikeService;
import com.bubbaTech.api.like.Ratings;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import lombok.AllArgsConstructor;
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
@RequestMapping("/app")
public class AppController {
    UserService userService;
    ClothingService clothingService;
    LikeService likeService;

    public static int CLOTHING_COUNT = 10;
    public static int PAGE_SIZE = 10;

    //Clothing card for user based on sessionId
    @GetMapping(value = "/card", produces = "application/json")
    public EntityModel<ClothingDTO> card(
            Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        ClothingDTO response = clothingService.getCard(this.getUserId(principal), typeFilter, genderFilter);
        response.reverseImageList();

        return EntityModel.of(response);
    }

    @RequestMapping(value = "/card", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsRequest() {
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/cardList", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> getCardList(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        List<Clothing> items = clothingService.recommendClothingIdList(this.getUserId(principal), typeFilter, genderFilter);
        List<EntityModel<ClothingDTO>> itemsDTO = new ArrayList<>();
        for (Clothing item : items) {
            itemsDTO.add(EntityModel.of(item));
        }

        return CollectionModel.of(itemsDTO);
    }

    //Liked list for user based on sessionId
    @GetMapping(value = "/likes", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> likes(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) Integer pageNumber) {
        return getClothingList(this.getUserId(principal), ClothingListType.LIKE, typeFilter, genderFilter, pageNumber);
    }

    //Collection for user based on sessionId
    @GetMapping(value = "/collection", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> collection(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) int pageNumber) {
        //TODO: Return by page
        return getClothingList(this.getUserId(principal), ClothingListType.BOUGHT, typeFilter, genderFilter, pageNumber);
    }

    //Deals with app like
    @PostMapping(value = "/like", produces = "application/json")
    public ResponseEntity<?> like(@RequestBody LikeDTO newLike, Principal principal){
        long userId = getUserId(principal);
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(userService.getById(userId));
        //Sets like to like rating + imageTapRatio
        newLike.setRating(Ratings.LIKE_RATING + min(newLike.getImageTapsRatio() * Ratings.TOTAL_IMAGE_TAP_RATING, Ratings.TOTAL_IMAGE_TAP_RATING));
        newLike.setLiked(true);

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));

        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/dislike", produces = "application/json")
    public ResponseEntity<?> dislike(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(userService.getById(userId));

        //Sets like to dislike rating
        newLike.setRating(Ratings.DISLIKE_RATING);
        newLike.setLiked(false);

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));

        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/removeLike", produces = "application/json")
    public ResponseEntity<?> removeLike(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(userService.getById(userId));

        //Sets like to remove like rating
        newLike.setRating(Ratings.REMOVE_LIKE_RATING);
        newLike.setLiked(false);
        Optional<Like> findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(),newLike.getUser().getId());
        if (findLike.isPresent()) {
            newLike.setBought(findLike.get().isBought());
        } else {
            newLike.setBought(false);
        }

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));

        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/bought", produces = "application/json")
    public ResponseEntity<?> bought(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(userService.getById(userId));

        //Sets like to like rating + imageTapRatio
        newLike.setRating(Ratings.BUY_RATING);
        Optional<Like> findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(),newLike.getUser().getId());
        if (findLike.isPresent()) {
            newLike.setLiked(findLike.get().isLiked());
        } else {
            newLike.setLiked(false);
        }
        newLike.setBought(true);

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));

        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/pageClick", produces = "application/json")
    public ResponseEntity<?> pageClick(@RequestBody LikeDTO newLike, Principal principal) {
        long userId = getUserId(principal);
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(userService.getById(userId));

        newLike.setRating(Ratings.PAGE_CLICK_RATING);
        Optional<Like> findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(),newLike.getUser().getId());
        if (findLike.isPresent()) {
            newLike.setLiked(findLike.get().isLiked());
            newLike.setBought(findLike.get().isBought());
        } else {
            newLike.setLiked(false);
            newLike.setBought(false);
        }

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));

        return ResponseEntity.ok().body(like);
    }

    @GetMapping(value="/checkToken")
    public ResponseEntity<?> checkToken(Principal principal) {
        long userId = getUserId(principal);
        userService.updateLastLogin(userId);

        return ResponseEntity.ok().build();
    }


    private CollectionModel<EntityModel<ClothingDTO>> getClothingList(long userId, ClothingListType listType, String typeFilter, String genderFilter, Integer pageNumber) {
        List<Like> likes = likeService.getAllByUserId(userId, listType, typeFilter, genderFilter, pageNumber);

        List<EntityModel<ClothingDTO>> items = new ArrayList<>();
        for (Like like : likes) {
            ClothingDTO item = clothingService.getById(like.getClothing().getId());
            item.reverseImageList();
            items.add(EntityModel.of(item));
        }

        return CollectionModel.of(items);
    }

    private long getUserId(Principal principal) {
        UserDTO user = userService.getByUsername(principal.getName());
        return user.getId();
    }
}
