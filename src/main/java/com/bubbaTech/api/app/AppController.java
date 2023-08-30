//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.app;

import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.clothing.ClothingListType;
import com.bubbaTech.api.clothing.ClothingService;
import com.bubbaTech.api.info.ServiceLogger;
import com.bubbaTech.api.like.LikeDTO;
import com.bubbaTech.api.like.LikeNotFoundException;
import com.bubbaTech.api.like.LikeService;
import com.bubbaTech.api.like.Ratings;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.min;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app")
public class AppController {

    @NonNull
    UserService userService;
    @NonNull
    ClothingService clothingService;
    @NonNull
    LikeService likeService;
    @NonNull
    private final ServiceLogger logger;
    public static int PAGE_SIZE = 10;

    @Value("${system.image_processing_addr}")
    private String imageProcessingAddr;


    //Clothing card for user based on sessionId
    @GetMapping(value = "/card", produces = "application/json")
    public EntityModel<ClothingDTO> card(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        ClothingDTO response = clothingService.getCard(this.getUserId(principal), typeFilter, genderFilter);
        response.reverseImageList();

        return EntityModel.of(response);
    }

    @RequestMapping(value = "/card", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsRequest() {
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/cardList", produces = "application/json")
    public CollectionModel<EntityModel<?>> getCardList(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        List<ClothingDTO> items = clothingService.recommendClothingList(this.getUserId(principal), typeFilter, genderFilter, false);
        List<EntityModel<?>> entityModelList = new ArrayList<>();
        try {
            for (ClothingDTO item : items) {
                entityModelList.add(EntityModel.of(item));
            }

            return CollectionModel.of(entityModelList);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return CollectionModel.of(entityModelList);
        }
    }

    //Liked list for user based on sessionId
    @GetMapping(value = "/likes", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> likes(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) Integer pageNumber) {
        return getClothingList(this.getUserId(principal), ClothingListType.LIKE, typeFilter, genderFilter, pageNumber);
    }

    //Collection for user based on sessionId
    @GetMapping(value = "/collection", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> collection(Principal principal, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) int pageNumber) {
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
        try {
            LikeDTO findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(), newLike.getUser().getId());
            newLike.setBought(findLike.isBought());
        } catch (LikeNotFoundException exception) {
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

        try {
            LikeDTO findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(),newLike.getUser().getId());
            newLike.setLiked(findLike.isLiked());
        } catch (LikeNotFoundException exception) {
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
        try {
            LikeDTO findLike = likeService.findByClothingAndUser(newLike.getClothing().getId(),newLike.getUser().getId());
            newLike.setLiked(findLike.isLiked());
            newLike.setBought(findLike.isBought());
        } catch (LikeNotFoundException exception) {
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

    @GetMapping(value="/image")
    public ResponseEntity<byte[]> redirectToImageProcessing(@RequestParam(value = "clothingId", required = true) int clothingId, @RequestParam(value = "imageId", required = true) int imageId) {
        try {
            String redirectUrl = "http://" + imageProcessingAddr + "/image?clothingId=" + clothingId + "&imageId=" + imageId;
            URL url = new URL(redirectUrl);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(redirectUrl, byte[].class);

            return ResponseEntity.ok().contentType(Objects.requireNonNull(response.getHeaders().getContentType())).body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping(value="/images", produces = "application/json")
    public ResponseEntity<?> beginImageProcessing(@RequestParam(value="clothingId", required = true) int clothingId) {
        try {
            String redirectUrl = "http://" + imageProcessingAddr + "/images?clothingId=" + clothingId;
            URL url = new URL(redirectUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                String errorMessage = "Unable to connect to image processing system.";
                logger.error(errorMessage);
                throw new Exception(errorMessage);
            }

            JSONObject jsonResponse = ClothingService.getConnectionResponse(connection);
            JSONArray imageUrls = (JSONArray) jsonResponse.get("imageUrls");

            for (int i = 0; i < imageUrls.size(); i++) {
                imageUrls.add(i, linkTo(AppController.class)
                        .slash(imageUrls.get(i).toString())
                        .toUriComponentsBuilder().scheme("https").toUriString());
            }
            return ResponseEntity.ok().body(imageUrls);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value="/filterOptions", produces = "application/json")
    public ResponseEntity<?> filterOptions() {
        return ResponseEntity
                .ok()
                .body(clothingService.getFilterOptions());
    }

    private CollectionModel<EntityModel<ClothingDTO>> getClothingList(long userId, ClothingListType listType, String typeFilter, String genderFilter, Integer pageNumber) {
        List<LikeDTO> likes = likeService.getAllByUserId(userId, listType, typeFilter, genderFilter, pageNumber);

        List<EntityModel<ClothingDTO>> items = new ArrayList<>();
        for (LikeDTO like : likes) {
            ClothingDTO item = clothingService.getById(like.getClothing().getId());
            List<String> imageUrls = item.getImageURL();
            //TODO: Consolidate images into one serializer
            item.setImageURL(imageUrls.subList(imageUrls.size() - 2, imageUrls.size() - 1));
            items.add(EntityModel.of(item));
        }

        return CollectionModel.of(items);
    }

    private long getUserId(Principal principal) {
        UserDTO user = userService.getByUsername(principal.getName());
        return user.getId();
    }
}
