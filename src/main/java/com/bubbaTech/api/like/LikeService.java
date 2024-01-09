//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.app.AppController;
import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.Clothing;
import com.bubbaTech.api.clothing.ClothingListType;
import com.bubbaTech.api.clothing.ClothingTag;
import com.bubbaTech.api.filterOptions.FilterOptionsDTO;
import com.bubbaTech.api.mapping.Mapper;
import com.bubbaTech.api.user.Gender;
import com.bubbaTech.api.user.User;
import jakarta.transaction.Transactional;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
@Transactional
public class LikeService {
    private final LikeRepository repository;
    private final Mapper mapper;

    @Value("${system.recommendation_system_addr}")
    public String recommendationSystemAddr;
    public LikeService(LikeRepository repository, Mapper mapper) {
        super();
        this.repository = repository;
        this.mapper = mapper;
    }

    public LikeDTO update(LikeDTO likeRequest) {
        Like like = repository.findByClothingAndUser(likeRequest.getClothing().getId(), likeRequest.getUser().getId()).orElseThrow(() -> new LikeNotFoundException(likeRequest.getId()));

        like.setBought(likeRequest.isBought());
        like.setLiked(likeRequest.isLiked());
        like.setRating(likeRequest.getRating() + like.getRating());
        like = repository.save(like);
        LikeDTO returnLikeDTO = mapper.likeToLikeDTO(like);
        sendLike(returnLikeDTO);
        return returnLikeDTO;
    }

    public LikeDTO create(LikeDTO likeRequest) {
        try {
            //Updates like if like with clothing and user already exists.
            LikeDTO foundLike = findByClothingAndUser(likeRequest.getClothing().getId(), likeRequest.getUser().getId());
            likeRequest.setId(foundLike.getId());
            return update(likeRequest);
        } catch (LikeNotFoundException exception) {
            // Converts likeRequest to like entity.
            Clothing likedClothing = mapper.clothingDTOToClothing(likeRequest.getClothing());
            User likedUser = mapper.userDTOToUser(likeRequest.getUser());
            Like like = new Like(likeRequest, likedUser, likedClothing);
            LikeDTO likeDTO = mapper.likeToLikeDTO(repository.save(like));
            // Sends like to recommender service
            sendLike(likeDTO);
            return likeDTO;
        }
    }

    public Long getPageCount(long userId, ClothingListType listType, String typeFilter, String genderFilter) {
        //Convert genderFilter to gender
        Gender gender = null;
        if (genderFilter != null) {
            gender = Gender.stringToGender(genderFilter);
        }

        //Convert typeFilter to list of types
        List<ClothType> typeFilters = null;
        if (typeFilter != null) {
            typeFilters = new ArrayList<>();
            String[] filters = typeFilter.split(",");
            for (String str : filters) {
                typeFilters.add(ClothType.stringToClothType(str));
            }
        }

        boolean liked = false;
        boolean bought = false;
        switch (listType) {
            case LIKE -> liked = true;
            case BOUGHT -> {
                liked = true;
                bought = true;
            }
        }

        Long pageAmount;
        if (genderFilter != null && typeFilter != null) {
            pageAmount = repository.countAllByUserIdWithGenderAndTypes(userId, liked, bought, gender, typeFilters);
        } else if (genderFilter != null) {
            pageAmount = repository.countAllByUserIdWithGender(userId, liked, bought, gender);
        } else if (typeFilter != null) {
            pageAmount = repository.countAllByUserIdWithTypes(userId, liked, bought, typeFilters);
        } else {
            pageAmount = repository.countAllByUserId(userId, liked, bought);
        }

        return Long.valueOf((long) Math.ceil((double) pageAmount / (double) AppController.PAGE_SIZE));
    }

    public List<LikeDTO> getAllByUserId(long userId, ClothingListType listType, String typeFilter, String genderFilter, Integer pageNumber) {

        //Convert genderFilter to gender
        Gender gender = null;
        if (genderFilter != null) {
            gender = Gender.stringToGender(genderFilter);
        }

        //Convert typeFilter to list of types
        List<ClothType> typeFilters = null;
        if (typeFilter != null) {
            typeFilters = new ArrayList<>();
            String[] filters = typeFilter.split(",");
            for (String str : filters) {
                typeFilters.add(ClothType.stringToClothType(str));
            }
        }

        boolean liked = false;
        boolean bought = false;
        switch (listType) {
            case LIKE -> liked = true;
            case BOUGHT -> {
                liked = true;
                bought = true;
            }
        }

        Pageable pageRequest;
        if (pageNumber == null) {
            pageRequest = Pageable.unpaged();
        } else {
            pageRequest = PageRequest.of(pageNumber, AppController.PAGE_SIZE, Sort.by("id").descending());
        }

        Page<Like> likePage;
        if (genderFilter != null && typeFilter != null) {
            likePage = repository.findAllByUserIdWithGenderAndTypes(userId, liked, bought, gender, typeFilters, pageRequest);
        } else if (genderFilter != null) {
            likePage = repository.findAllByUserIdWithGender(userId, liked, bought, gender, pageRequest);
        } else if (typeFilter != null) {
            likePage = repository.findAllByUserIdWithTypes(userId, liked, bought, typeFilters, pageRequest);
        } else {
            likePage = repository.findAllByUserId(userId, liked, bought, pageRequest);
        }

        //Converts likes to likeDTO
        List<LikeDTO> likeDTOList = new ArrayList<>();
        for (Like like : likePage.getContent()) {
            likeDTOList.add(mapper.likeToLikeDTO(like));
        }
        return likeDTOList;
    }

    public LikeDTO findByClothingAndUser(long clothingId, long userId) throws LikeNotFoundException  {
        Optional<Like> like = repository.findByClothingAndUser(clothingId, userId);
        if (like.isEmpty())
            throw new LikeNotFoundException(String.format("No like found with clothingId %d and userId %d", clothingId, userId));

        return mapper.likeToLikeDTO(like.get());
    }

    /**
     * Gets custom filter options by the items liked for the userId.
     * @param userId: A long representing the userId querying likes for.
     * @return: A filter options DTO with the corresponding types.
     */
    public FilterOptionsDTO getFilterOptionsByLikes(long userId) {
        Pageable pageRequest = Pageable.unpaged();;
        Map<Gender, Map<ClothType, List<ClothingTag>>> filterInformation = new HashMap<>();
        List<Gender> genders = repository.getAllUniqueGenders(userId);
        for (Gender gender: genders) {
            Map<ClothType, List<ClothingTag>> tagMap = new HashMap<>();
            List<ClothType> uniqueTypes = repository.getAllUniqueTypesByGender(userId, gender);
            for (ClothType type : uniqueTypes) {
                tagMap.put(type, repository.getAllUniqueTagsByTypeAndGender(userId, gender, type));
            }
            filterInformation.put(gender, tagMap);
        }

        return new FilterOptionsDTO(filterInformation);
    }

    @Async
    public void sendLike(LikeDTO like) {
        try {
            URL url = new URL("http://" + recommendationSystemAddr + "/like");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json");
            connection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", like.getUser().getId());
            jsonObject.put("clothingId", like.getClothing().getId());
            jsonObject.put("rating", like.getRating());
            outputStream.writeBytes(jsonObject.toJSONString());

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Did not successfully send like with URL http://" + recommendationSystemAddr + "/like and data " + jsonObject.toJSONString() + ".");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
