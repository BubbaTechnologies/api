//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.app;

import com.bubbaTech.api.actuator.LikeDataEndpoint;
import com.bubbaTech.api.actuator.RouteResponseTimeEndpoint;
import com.bubbaTech.api.app.responseObjects.clothingListResponse.ActivityLikeDTO;
import com.bubbaTech.api.app.responseObjects.clothingListResponse.ActivityListResponse;
import com.bubbaTech.api.app.responseObjects.clothingListResponse.ClothingListResponse;
import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.clothing.ClothingListType;
import com.bubbaTech.api.clothing.ClothingService;
import com.bubbaTech.api.filterOptions.FilterOptionsDTO;
import com.bubbaTech.api.info.ServiceLogger;
import com.bubbaTech.api.like.LikeDTO;
import com.bubbaTech.api.like.LikeNotFoundException;
import com.bubbaTech.api.like.LikeService;
import com.bubbaTech.api.like.Ratings;
import com.bubbaTech.api.security.authentication.JwtUtil;
import com.bubbaTech.api.security.authentication.model.AuthenticationResponse;
import com.bubbaTech.api.user.*;
import com.bubbaTech.api.user.metricStructs.SessionDataDTO;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app")
public class AppController {
    @NonNull
    private JwtUtil jwt;
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

    @NonNull
    private final LikeDataEndpoint likeDataEndpoint;

    @NonNull
    private final RouteResponseTimeEndpoint routeResponseTimeEndpoint;


    /**
     * @param principal - The user requesting.
     * @param request - The request.
     * @param typeFilter - The filter of clothingType being applied.
     * @param genderFilter - The filter of gender being applied.
     * @return - A response entity of the clothingDTO.
     */
    @GetMapping(value = "/card", produces = "application/json")
    public ResponseEntity<ClothingDTO> card(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        long startTime = System.currentTimeMillis();
        ClothingDTO response = clothingService.getCard(this.getUserId(principal), typeFilter, genderFilter);
        response.reverseImageList();
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(response);
    }

    /**
     * Provides server status to options request.
     * @return - Returns server status.
     */
    @RequestMapping(value = "/card", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsRequest() {
        return ResponseEntity.ok().build();
    }

    /**
     *
     * @param principal - User requesting.
     * @param request - Request information.
     * @param typeFilter - Clothing type filter applied to query.
     * @param genderFilter - Gender filter applied to query.
     * @return - A response entity representing the cardList:
     *  {
     *      "clothingList":[clothingDTO],
     *      "totalPageCount":-1
     *  }
     */
    @GetMapping(value = "/cardList", produces = "application/json")
    public ResponseEntity<?> getCardList(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter) {
        long startTime = System.currentTimeMillis();
        List<ClothingDTO> items = clothingService.recommendClothingList(this.getUserId(principal), typeFilter, genderFilter, false);
        ClothingListResponse response = new ClothingListResponse(items, (long) -1);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(response);
    }

    /**
     * Provides preview clothing.
     * @param request - Request information.
     * @return - ResponseEntity containing the ClothingListResponse.
     */
    @GetMapping("/preview")
    public ResponseEntity<ClothingListResponse> previewList(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        List<ClothingDTO> items = clothingService.getPreviewClothing();
        ClothingListResponse response = new ClothingListResponse(items, (long) -1);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(response);
    }

    @Deprecated
    @GetMapping(value = "/totalPages", produces="application/json")
    public ResponseEntity<?> getTotalPages(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value="queryType", required = true) String queryType) {
        long startTime = System.currentTimeMillis();

        //Converts ClothingListType
        ClothingListType listType;
        switch (queryType.toLowerCase()) {
            case "likes":
                listType = ClothingListType.LIKE;
                break;
            case "collection":
                listType = ClothingListType.BOUGHT;
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        long totalPages = likeService.getPageCount(this.getUserId(principal), listType, typeFilter, genderFilter);
        Map<String, Long> response = new HashMap<>();
        response.put("totalPages", totalPages);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(response);
    }


    /**
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param typeFilter: The type filter for the type of likes requested.
     * @param genderFilter: The gender filter for the type of likes requested.
     * @param pageNumber: The page number requested.
     * @return:
     * {
     *      "clothingList": [
     *             {
     *                 "id":int,
     *                 "name":str,
     *                 "imageURL":[str],
     *                 "productURL":str,
     *                 "store": {
     *                     "id":int,
     *                     "name":str,
     *                     "url":str
     *                 },
     *                 "type":str,
     *                 "gender":str,
     *                 "date":str
     *             }
     *         ],
     *      "totalPageCount": Long
     * }
     */
    @GetMapping(value = "/likes", produces = "application/json")
    public ResponseEntity<?> likes(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) Integer pageNumber) {
        long startTime = System.currentTimeMillis();
        List<ClothingDTO> likesList = getClothingListAsList(this.getUserId(principal), ClothingListType.LIKE, typeFilter, genderFilter, pageNumber);
        long totalPages = likeService.getPageCount(this.getUserId(principal), ClothingListType.LIKE, typeFilter, genderFilter);

        ClothingListResponse responseObject = new ClothingListResponse(likesList, totalPages);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(responseObject);
    }

    /**
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param typeFilter: The type filter for the type of collection requested.
     * @param genderFilter: The gender filter for the type of likes requested.
     * @param pageNumber: The page number requested.
     * @return:
     * {
     *     "_embedded": {
     *         "clothingDTOList": [
     *             {
     *                 "id":int,
     *                 "name":str,
     *                 "imageURL": [str],
     *                 "productURL": str,
     *                 "store": {
     *                     "id": int,
     *                     "name": str,
     *                     "url": str
     *                 },
     *                 "type": str,
     *                 "gender": str,
     *                 "date":str
     *             }
     *         ]
     *     }
     * }
     */
    @GetMapping(value = "/collection", produces = "application/json")
    public CollectionModel<EntityModel<ClothingDTO>> collection(Principal principal, HttpServletRequest request, @RequestParam(value = "type", required = false) String typeFilter, @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) int pageNumber) {
        long startTime = System.currentTimeMillis();
        CollectionModel<EntityModel<ClothingDTO>> collectionList = getClothingListAsCollectionModel(this.getUserId(principal), ClothingListType.BOUGHT, typeFilter, genderFilter, pageNumber);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return collectionList;
    }

    /**
     * @param newLike: A LikeDTO representing new like data. Check LikeDeserializer for communication information.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @return:
     * {
     *     "id": int,
     *     "date": string,
     *     "liked": bool,
     *     "bought": bool
     * }
     */
    @PostMapping(value = "/like", produces = "application/json")
    public ResponseEntity<?> like(@RequestBody LikeDTO newLike, HttpServletRequest request, Principal principal){
        long startTime = System.currentTimeMillis();
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));

        newLike.setUser(getUserDTO(principal));

        //Adds to like metrics
        likeDataEndpoint.addLikeAndRecommend();

        //Sets like to like rating + imageTapRatio
        newLike.setRating(Ratings.LIKE_RATING + min(newLike.getImageTaps() * Ratings.TOTAL_IMAGE_TAP_RATING, Ratings.TOTAL_IMAGE_TAP_RATING));
        newLike.setLiked(true);

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(like);
    }

    /**
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param newLike: LikeDTO representing new like data. Check LikeDeserializer for communication information.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @return:
     * {
     *     "id": int,
     *     "date": string,
     *     "liked": bool,
     *     "bought": bool
     * }
     */
    @PostMapping(value = "/dislike", produces = "application/json")
    public ResponseEntity<?> dislike(HttpServletRequest request, @RequestBody LikeDTO newLike, Principal principal) {
        long startTime = System.currentTimeMillis();
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(getUserDTO(principal));

        //Adds to like metrics
        likeDataEndpoint.addRecommend();

        //Sets like to dislike rating
        newLike.setRating(Ratings.DISLIKE_RATING);
        newLike.setLiked(false);

        EntityModel<LikeDTO> like = EntityModel.of(likeService.create(newLike));

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(like);
    }

    /**
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param newLike: LikeDTO representing new like data. Check LikeDeserializer for communication information.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @return:
     * {
     *     "id": int,
     *     "date": string,
     *     "liked": bool,
     *     "bought": bool
     * }
     */
    @PostMapping(value = "/removeLike", produces = "application/json")
    public ResponseEntity<?> removeLike(HttpServletRequest request, @RequestBody LikeDTO newLike, Principal principal) {
        long startTime = System.currentTimeMillis();
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(getUserDTO(principal));

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

    /**
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param newLike: LikeDTO representing new like data. Check LikeDeserializer for communication information.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @return:
     * {
     *     "id": int,
     *     "date": string,
     *     "liked": bool,
     *     "bought": bool
     * }
     */
    @PostMapping(value = "/bought", produces = "application/json")
    public ResponseEntity<?> bought(HttpServletRequest request, @RequestBody LikeDTO newLike, Principal principal) {
        long startTime = System.currentTimeMillis();
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(getUserDTO(principal));

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

    /**
     * @param newLike: LikeDTO representing new like data. Check LikeDeserializer for communication information.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @return:
     * {
     *     "id": int,
     *     "date": string,
     *     "liked": bool,
     *     "bought": bool
     * }
     */
    @PostMapping(value = "/pageClick", produces = "application/json")
    public ResponseEntity<?> pageClick(HttpServletRequest request, @RequestBody LikeDTO newLike, Principal principal) {
        long startTime = System.currentTimeMillis();
        newLike.setClothing(clothingService.getById(newLike.getClothing().getId()));
        newLike.setUser(getUserDTO(principal));

        //Adds like data metrics
        likeDataEndpoint.addPageClick(newLike.getClothing().getStore().getName());

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

    /**
     * Adds metrics to the system when the application is closed.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param sessionData: Data formatted as follows:
     *                   {
     *                   "sessionLength": str (formatted as HH:MM:SS)
     *                   }
     * @return: 200 if successful.
     */
    @PostMapping(value = "/sessionData")
    public ResponseEntity<?> closeApp(HttpServletRequest request, Principal principal, @RequestBody Map<String, ?> sessionData) {
        long startTime = System.currentTimeMillis();

        String[] expectedKeys = {"sessionLength"};
        for (String key : expectedKeys) {
            if (!sessionData.containsKey(key)) {
                return ResponseEntity.unprocessableEntity().build();
            }
        }

        //Clean data from sessionLength
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime time = LocalTime.parse((String) sessionData.get("sessionLength"), formatter);

        SessionDataDTO sessionDataDTO = new SessionDataDTO(time.format(formatter), getUserDTO(principal));

        userService.saveSession(sessionDataDTO);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    /**
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @return: A 200 response if valid token.
     */
    @GetMapping(value="/checkToken")
    public ResponseEntity<?> checkToken(HttpServletRequest request, Principal principal) {
        long startTime = System.currentTimeMillis();
        long userId = getUserId(principal);
        userService.updateLastLogin(userId);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    @Deprecated
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

    @Deprecated
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

    /**
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param filterType: The purpose of the filters (e.g. like page, activity page, etc.)
     * @return:
     * {
     *     "genders":[str],
     *     "types":[[str]],
     *     "tags": {
     *         type:[str]
     *     }
     * }
     */
    @GetMapping(value="/filterOptions", produces = "application/json")
    public ResponseEntity<?> filterOptions(HttpServletRequest request, Principal principal, @RequestParam(value="type", required = false) String filterType) {
        long startTime = System.currentTimeMillis();
        if (filterType == null) {
            filterType = "";
        }

        ResponseEntity<?> responseEntity = null;
        Long userId = getUserId(principal);

        switch (filterType.toLowerCase()) {
            case "likes":
                responseEntity = ResponseEntity.ok().body(likeService.getFilterOptionsByLikes(userId));
                routeResponseTimeEndpoint.addResponseTime(request.getRequestURI() + "/likes", System.currentTimeMillis() - startTime);
                break;
            case "activity":
                //Gets users that are followed.
                List<UserDTO> following = userService.getFollowing(userId);
                FilterOptionsDTO filterOptionsDTO = likeService.getFilterOptionsByUserIds(following.stream()
                        .map(UserDTO::getId)
                        .collect(Collectors.toList()));

                responseEntity = ResponseEntity.ok().body(filterOptionsDTO);
                routeResponseTimeEndpoint.addResponseTime(request.getRequestURI() + "/activity", System.currentTimeMillis() - startTime);
                break;
            default:
                responseEntity = ResponseEntity.ok().body(clothingService.getFilterOptions());
                routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
                break;
        }

        return responseEntity;
    }

    @PostMapping(value="/updateLocation")
    public ResponseEntity<?> updateLocation(HttpServletRequest request, Principal principal, @RequestBody Map<String, Double> locationInformation) {
        long startTime = System.currentTimeMillis();
        if (!(locationInformation.containsKey("latitude") && locationInformation.containsKey("longitude"))) {
            return ResponseEntity.unprocessableEntity().build();
        }
        UserDTO userDTO = getUserDTO(principal);
        userDTO.setLatitude(locationInformation.get("latitude"));
        userDTO.setLongitude(locationInformation.get("longitude"));
        userService.update(userDTO);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/updateDeviceId")
    public ResponseEntity<?> updateDeviceId(HttpServletRequest request, Principal principal, @RequestBody Map<String, String> information) {
        long startTime = System.currentTimeMillis();
        if (!(information.containsKey("deviceId"))) {
            return ResponseEntity.unprocessableEntity().build();
        }
        UserDTO userDTO = getUserDTO(principal);
        userDTO.setDeviceId(information.get("deviceId"));
        userService.update(userDTO);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/delete")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, Principal principal) {
        long startTime = System.currentTimeMillis();

        UserDTO userDTO = getUserDTO(principal);
        userDTO.setEnabled(false);
        userService.update(userDTO);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<?> activate(HttpServletRequest request, Principal principal) {
        //How can a user that is not active, activate their account with this type of request?
        long startTime = System.currentTimeMillis();

        UserDTO userDTO = getUserDTO(principal);
        userDTO.setEnabled(true);
        userService.update(userDTO);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns current user account information.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @return:
     * {
     *     "username": str,
     *     "email": str,
     *     "birthdate": str,
     *     "gender": str,
     *     "privateAccount": bool
     * }
     */
    @GetMapping("/userInfo")
    public ResponseEntity<?> userInfo(HttpServletRequest request, Principal principal) {
        long startTime = System.currentTimeMillis();
        UserDTO userDTO = getUserDTO(principal);

        //Maps the important fields
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("username", userDTO.getUsername());
        responseMap.put("email", userDTO.getEmail());
        responseMap.put("birthdate", userDTO.getBirthDate().toString());
        responseMap.put("gender", userDTO.getGender().getStringValue());
        responseMap.put("privateAccount", userDTO.getPrivateAccount());

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().body(responseMap);
    }

    /**
     * Updates user information.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param userInfo: New user information to be updated with.
     * @return: {
     *     "email":String,
     *     "jwt" String
     * }.
     */
    @PostMapping("/updateUserInfo")
    public ResponseEntity<?> updateUserInfo(HttpServletRequest request, Principal principal, @RequestBody Map<String, ?> userInfo) {
        long startTime = System.currentTimeMillis();
        UserDTO userDTO = getUserDTO(principal);

        userDTO.setUsername(userInfo.get("username").toString());
        userDTO.setEmail(userInfo.get("email").toString());

        //Checks if null and lets string be null
        String birthdate = (userInfo.get("birthdate") == null) ? null : userInfo.get("birthdate").toString();
        userDTO.setBirthDate(UserDeserializer.getBirthdate(birthdate));
        userDTO.setGender(UserDeserializer.getGender(userInfo.get("gender").toString()));
        userDTO.setPrivateAccount(Boolean.valueOf(userInfo.get("privateAccount").toString()));

        if (userInfo.containsKey("password")) {
            userDTO.setPassword(userInfo.get("password").toString());
        }

        userDTO = userService.update(userDTO);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);

        return ResponseEntity.ok(new AuthenticationResponse(jwt.generateToken(userDTO), userDTO.getEmail()));
    }

    /**
     * Updates user's password.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param info:
     * {
     *      "password": str
     * }
     * @return: 200 if successful.
     */
    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(HttpServletRequest request, Principal principal, Map<String, String> info) {
        

        long startTime = System.currentTimeMillis();
        UserDTO userDTO = getUserDTO(principal);

        userDTO.setPassword(userService.encodePassword(info.get("password")));
        userService.update(userDTO);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }


    /**
     * Returns activity feed for user in pages.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param typeFilter: The type of clothing being filtered in the feed.
     * @param genderFilter: The type of gender being filtered in the feed.
     * @param pageNumber: The page number of feed being requested.
     * @return:
     * {
     *     "activityList": [
     *          "userProfile": {
     *              "id": Long,
     *              "username":str,
     *              "privateAccount":bool,
     *              "followingStatus": Int
     *          },
     *          "clothingDTO": {
     *              "id": long,
     *              "name": str,
     *              "imageURL": [str],
     *              "productURL": str,
     *              "store":{
     *                  "id": long,
     *                  "name": str,
     *                  "enabled": bool,
     *                  "url": str
     *              }
     *              "type": str,
     *              "gender": str,
     *              "date": str
     *          }
     *     ]
     *     "totalPages": Long
     * }
     */
    @GetMapping("/activity")
    public ResponseEntity<?> activity(HttpServletRequest request, Principal principal, @RequestParam(value = "type", required = false) String typeFilter,
                                      @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) Integer pageNumber) {
        long startTime = System.currentTimeMillis();
        Long userId = getUserId(principal);

        //Gets users followed by requester.
        List<UserDTO> following = userService.getFollowing(userId);

        Long pageCount = likeService.getActivityPageCount(following.stream()
                .map(UserDTO::getId)
                .collect(Collectors.toList()), typeFilter, genderFilter);
        List<LikeDTO> activity = likeService.getActivity(following.stream()
                .map(UserDTO::getId)
                .collect(Collectors.toList()), typeFilter, genderFilter, pageNumber);
        List<ActivityLikeDTO> activityLikeDTOList = new ArrayList<>();

        for (LikeDTO likeDTO : activity) {
            activityLikeDTOList.add(new ActivityLikeDTO(new ProfileDTO(likeDTO.getUser(), FollowingStatus.FOLLOWING), likeDTO.getClothing()));
        }
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(new ActivityListResponse(activityLikeDTOList, pageCount));
    }

    /**
     * Returns the user requested profiles information.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param userId: A Long the represents the userId of the user being requested.
     * @return:
     * {
     *     "id":int,
     *     "username":str,
     *     "privateAccount": bool
     *     "followingStatus": Int
     * }
     */
    @GetMapping("/profileInfo")
    public ResponseEntity<ProfileDTO> profile(HttpServletRequest request, Principal principal, @RequestParam(value = "userId") Long userId) {
        System.out.println(userService.encodePassword("testPassword-1218"));
        long startTime = System.currentTimeMillis();

        UserDTO userDTO = userService.getById(userId);
        FollowingStatus followingStatus = userService.getFollowingRelation(getUserId(principal), userDTO.getId());

        ProfileDTO profileDTO = new ProfileDTO(userDTO.getId(), userDTO.getUsername(), userDTO.getPrivateAccount(), followingStatus);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(profileDTO);
    }

    /**
     * Gets profile activity section.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param userId: The user id being requested.
     * @param typeFilter: The type of clothing being filtered.
     * @param genderFilter: The gender of clothing being filtered.
     * @param pageNumber: The page number being requested.
     * @return: 403 if prinicpal cannot access information else:
     * {
     *     "pageCount": Long.
     *     "clothingList": [{
     *       "id":int,
     *       "name":str,
     *        "imageURL":[str],
     *        "productURL":str,
     *        "store":
     *              {
     *              "id":int,
     *              "name":str,
     *              "url":str
     *              },
     *        "type":str,
     *        "gender":str,
     *        "date":str
     *     }]
     * }
     */
    @GetMapping("/profileActivity")
    public ResponseEntity<?> profileActivity(HttpServletRequest request, Principal principal, @RequestParam(value = "userId") Long userId, @RequestParam(value = "type", required = false) String typeFilter,
                                             @RequestParam(value = "gender", required = false) String genderFilter, @RequestParam(value = "page", required = false) Integer pageNumber) {
        long startTime = System.currentTimeMillis();

        //Check if user has permission to get userId activity.
        UserDTO userDTO = getUserDTO(principal);
        UserDTO requestedUserDTO = userService.getById(userId);
        if (!userService.checkFollow(userDTO.getId(), userId) && requestedUserDTO.getPrivateAccount()) {
            return ResponseEntity.status(401).build();
        }

        long totalPages = likeService.getPageCount(userId, ClothingListType.LIKE, typeFilter, genderFilter);
        List<ClothingDTO> likesList = getClothingListAsList(userId, ClothingListType.LIKE, typeFilter, genderFilter, pageNumber);
        ClothingListResponse responseObject = new ClothingListResponse(likesList, totalPages);
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(responseObject);
    }

    /**
     * Returns a list of profiles of the user request to follow the principal.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @return:
     * {
     *      "profiles": [{
     *     "id":int,
     *     "username":str,
     *     "privateAccount": bool
     *     "followingStatus": Int
     *     }]
     * }
     */
    @GetMapping("/followRequests")
    public ResponseEntity<?> getFollowReqeusts(HttpServletRequest request, Principal principal) {
        long startTime = System.currentTimeMillis();

        Long userId = getUserId(principal);
        List<ProfileDTO> profileDTOS = userService.getRequested(userId);
        Map<String, List<ProfileDTO>> responseEntity = new HashMap<>();
        responseEntity.put("profiles", profileDTOS);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(responseEntity);
    }

    /**
     * Finds similar users to the searchQuery.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param searchQuery: A string representing the search.
     * @return:
     * {
     *      "profiles": [{
     *     "id":int,
     *     "username":str,
     *     "privateAccount": bool
     *     "followingStatus": Int
     *     }]
     * }
     */
    @GetMapping("/searchProfiles")
    public ResponseEntity<?> searchProfiles(HttpServletRequest request,Principal principal, @RequestParam(value = "query") String searchQuery) {
        long startTime = System.currentTimeMillis();

        List<ProfileDTO> profiles = userService.searchUsers(searchQuery, getUserId(principal));
        Map<String, List<ProfileDTO>> responseEntity = new HashMap<>();
        responseEntity.put("profiles", profiles);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);

        return ResponseEntity.ok(responseEntity);
    }

    /**
     * The requesting user follows the requested user. If requested is private, will request.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param followInformation: A map containing information about the requested user.
     * @return: 200 if successful.
     */
    @PostMapping("/follow")
    public ResponseEntity<?> follow(HttpServletRequest request, Principal principal, @RequestBody Map<String, Long> followInformation) {
        long startTime = System.currentTimeMillis();

        if (!followInformation.containsKey("userId")) {
            return ResponseEntity.badRequest().build();
        }

        userService.followUser(getUserId(principal), followInformation.get("userId"));

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    /**
     * Unfollows the requester from the requested. If requested, unrequests.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param followInformation: A map containing information about the requested user.
     * @return: 200 if successful.
     */
    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollow(HttpServletRequest request, Principal principal, @RequestBody Map<String, Long> followInformation) {
        long startTime = System.currentTimeMillis();

        if (!followInformation.containsKey("userId")) {
            return ResponseEntity.badRequest().build();
        }

        userService.unfollowUser(getUserId(principal), followInformation.get("userId"));
        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the follow request status.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param principal: Gives information about the requester. Check Principal type for references.
     * @param followInformation: A map containing information about the requested user and the new status.
     * @return: 200 if successful.
     */
    @PostMapping("/followRequestAction")
    public ResponseEntity<?> updateFollow(HttpServletRequest request, Principal principal, @RequestBody Map<String, ?> followInformation) {
        long startTime = System.currentTimeMillis();
        if (!followInformation.containsKey("userId") && !followInformation.containsKey("approve")) {
            return ResponseEntity.badRequest().build();
        }

        Boolean approve = (Boolean) followInformation.get("approve");
        if (!userService.requestAction(getUserId(principal), ((Integer) followInformation.get("userId")).longValue(), approve)) {
            return ResponseEntity.badRequest().build();
        }

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    /**
     * Checks if username is available.
     * @param request: Information about the request. Check HttpServletRequest for references.
     * @param username: Username being queried.
     * @return: 200 if successful.
     */
    @GetMapping("/checkUsername")
    public ResponseEntity<?> checkUsername(HttpServletRequest request, @RequestParam("username") String username) {
        long startTime = System.currentTimeMillis();

        userService.checkUsername(username);

        routeResponseTimeEndpoint.addResponseTime(request.getRequestURI(), System.currentTimeMillis() - startTime);
        return ResponseEntity.ok().build();
    }

    /**
     * @param userId: A long representing the user's id.
     * @param listType: A ClothingListType that represents which like is being queried.
     * @param typeFilter: A string representing the typeFilter applied to the query.
     * @param genderFilter: A string representing the genderFilter applied to the query.
     * @param pageNumber: An integer representing the pageNumber being queried.
     * @return: A list of ClothingDTO representing the clothing of listType queried.
     */
    private List<ClothingDTO> getClothingListAsList(long userId, ClothingListType listType, String typeFilter, String genderFilter, Integer pageNumber) {
        List<LikeDTO> likes = likeService.getAllByUserId(userId, listType, typeFilter, genderFilter, pageNumber);
        List<Long> ids = new ArrayList<>();
        for (LikeDTO like : likes)
            ids.add(like.getClothing().getId());

        List<ClothingDTO> items = clothingService.getByIds(ids);

        return items;
    }

    private CollectionModel<EntityModel<ClothingDTO>> getClothingListAsCollectionModel(long userId, ClothingListType listType, String typeFilter, String genderFilter, Integer pageNumber) {
        List<LikeDTO> likes = likeService.getAllByUserId(userId, listType, typeFilter, genderFilter, pageNumber);
        List<Long> ids = new ArrayList<>();
        for (LikeDTO like : likes)
            ids.add(like.getClothing().getId());

        List<ClothingDTO> items = clothingService.getByIds(ids);

        List<EntityModel<ClothingDTO>> entityModelList = new ArrayList<>();
        for (ClothingDTO item : items) {
            entityModelList.add(EntityModel.of(item));
        }

        return CollectionModel.of(entityModelList);
    }

    /**
     * @param principal: The user making the request.
     * @return: A long representing the user id.
     */
    private long getUserId(Principal principal) {
        UserDTO user = userService.getByUsername(principal.getName());
        return user.getId();
    }

    private UserDTO getUserDTO(Principal principal) {
        return userService.getByUsername(principal.getName());
    }
}
