//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.app;

import com.bubbaTech.api.actuator.RouteResponseTimeEndpoint;
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
import jakarta.servlet.http.HttpServletRequest;
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

    private final RouteResponseTimeEndpoint routeResponseTimeEndpoint;


    //Clothing card for user based on sessionId
    @GetMapping(value = "/card", produces = "application/json")
    public EntityModel<ClothingDTO> card(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        long startTime = System.currentTimeMillis();
        ClothingDTO response = clothingService.getCard(this.getUserId(principal), typeFilter, genderFilter);
        response.reverseImageList();
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return EntityModel.of(response);
    }

    @RequestMapping(value = "/card", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsRequest() {
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/cardList", produces = "application/json")
    public CollectionModel<EntityModel<?>> getCardList(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        long startTime = System.currentTimeMillis();

        List<ClothingDTO> items = clothingService.recommendClothingList(this.getUserId(principal), typeFilter, genderFilter, false);
        List<EntityModel<?>> entityModelList = new ArrayList<>();
        try {
            for (ClothingDTO item : items) {
                entityModelList.add(EntityModel.of(item));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return CollectionModel.of(entityModelList);
    }

    //Liked list for user based on sessionId
    @GetMapping(value = "/likes", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> likes(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) Integer pageNumber) {
        long startTime = System.currentTimeMillis();
        CollectionModel<EntityModel<ClothingDTO>> likesList = getClothingList(this.getUserId(principal), ClothingListType.LIKE, typeFilter, genderFilter, pageNumber);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return likesList;
    }

    //Collection for user based on sessionId
    @GetMapping(value = "/collection", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> collection(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) int pageNumber) {
        long startTime = System.currentTimeMillis();
        CollectionModel<EntityModel<ClothingDTO>> collectionList = getClothingList(this.getUserId(principal), ClothingListType.BOUGHT, typeFilter, genderFilter, pageNumber);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return collectionList;
    }

    //Deals with app like
    @PostMapping(value = "/like", produces = "application/json")
    public ResponseEntity<?> like(@RequestBody LikeDTO newLike, HttpServletRequest request, Principal principal){
        long startTime = System.currentTimeMillis();
        long userId = getUserId(principal);
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(userService.getById(userId));

        //Sets like to like rating + imageTapRatio
        newLike.setRating(Ratings.LIKE_RATING + min(newLike.getImageTapsRatio() * Ratings.TOTAL_IMAGE_TAP_RATING, Ratings.TOTAL_IMAGE_TAP_RATING));
        newLike.setLiked(true);

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/dislike", produces = "application/json")
    public ResponseEntity<?> dislike(HttpServletRequest request, @RequestBody LikeDTO newLike, Principal principal) {
        long startTime = System.currentTimeMillis();
        long userId = getUserId(principal);
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(userService.getById(userId));

        //Sets like to dislike rating
        newLike.setRating(Ratings.DISLIKE_RATING);
        newLike.setLiked(false);

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/removeLike", produces = "application/json")
    public ResponseEntity<?> removeLike(HttpServletRequest request, @RequestBody LikeDTO newLike, Principal principal) {
        long startTime = System.currentTimeMillis();
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
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/bought", produces = "application/json")
    public ResponseEntity<?> bought(HttpServletRequest request, @RequestBody LikeDTO newLike, Principal principal) {
        long startTime = System.currentTimeMillis();
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
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(like);
    }

    @PostMapping(value = "/pageClick", produces = "application/json")
    public ResponseEntity<?> pageClick(HttpServletRequest request, @RequestBody LikeDTO newLike, Principal principal) {
        long startTime = System.currentTimeMillis();
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
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(like);
    }

    @GetMapping(value="/checkToken")
    public ResponseEntity<?> checkToken(HttpServletRequest request, Principal principal) {
        long startTime = System.currentTimeMillis();
        long userId = getUserId(principal);
        userService.updateLastLogin(userId);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value="/image")
    public ResponseEntity<byte[]> redirectToImageProcessing(HttpServletRequest request, @RequestParam(value = "clothingId", required = true) int clothingId, @RequestParam(value = "imageId", required = true) int imageId) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<byte[]> returnEntity;
        try {
            String redirectUrl = "http://" + imageProcessingAddr + "/image?clothingId=" + clothingId + "&imageId=" + imageId;
            URL url = new URL(redirectUrl);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(redirectUrl, byte[].class);

            returnEntity = ResponseEntity.ok().contentType(Objects.requireNonNull(response.getHeaders().getContentType())).body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            returnEntity = ResponseEntity.notFound().build();
        }
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return returnEntity;
    }


    @GetMapping(value="/images", produces = "application/json")
    public ResponseEntity<?> beginImageProcessing(HttpServletRequest request, @RequestParam(value="clothingId", required = true) int clothingId) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<?> responseEntity;
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
            responseEntity = ResponseEntity.ok().body(imageUrls);
        } catch (Exception e) {
            e.printStackTrace();
            responseEntity = ResponseEntity.internalServerError().build();
        }
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return responseEntity;
    }

    @GetMapping(value="/filterOptions", produces = "application/json")
    public ResponseEntity<?> filterOptions(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<?> responseEntity = ResponseEntity.ok().body(clothingService.getFilterOptions());
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return responseEntity;
    }

    private CollectionModel<EntityModel<ClothingDTO>> getClothingList(long userId, ClothingListType listType, String typeFilter, String genderFilter, Integer pageNumber) {
        List<LikeDTO> likes = likeService.getAllByUserId(userId, listType, typeFilter, genderFilter, pageNumber);
        List<Long> ids = new ArrayList<>();
        for (LikeDTO like : likes)
            ids.add(like.getId());

        List<ClothingDTO> items = clothingService.getByIds(ids);

        List<EntityModel<ClothingDTO>> entityModelList = new ArrayList<>();
        for (ClothingDTO item : items) {
            entityModelList.add(EntityModel.of(item));
        }

        return CollectionModel.of(entityModelList);
    }

    private long getUserId(Principal principal) {
        UserDTO user = userService.getByUsername(principal.getName());
        return user.getId();
    }
}
