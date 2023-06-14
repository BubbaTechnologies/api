//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.app.AppController;
import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.ClothingListType;
import com.bubbaTech.api.user.Gender;
import jakarta.transaction.Transactional;
import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.bubbaTech.api.clothing.ClothingService.toClothType;
import static com.bubbaTech.api.clothing.ClothingService.toGender;

@org.springframework.stereotype.Service
public class LikeService {
    private final LikeRepository repository;
    private final ModelMapper modelMapper;
    public LikeService(LikeRepository repository, ModelMapper modelMapper) {
        super();
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public LikeDTO update(LikeDTO likeRequest) {
        Like like = repository.findByClothingAndUser(likeRequest.getClothing().getId(), likeRequest.getUser().getId()).orElseThrow(() -> new LikeNotFoundException(likeRequest.getId()));

        like.setBought(likeRequest.isBought());
        like.setLiked(likeRequest.isLiked());
        like.setRating(likeRequest.getRating() + like.getRating());
        like = repository.save(like);
        LikeDTO returnLikeDTO = modelMapper.map(like, LikeDTO.class);
        sendLike(returnLikeDTO);
        return returnLikeDTO;
    }

    public LikeDTO create(LikeDTO likeRequest) {
        try {
            LikeDTO foundLike = findByClothingAndUser(likeRequest.getClothing().getId(), likeRequest.getUser().getId());
            likeRequest.setId(foundLike.getId());
            return update(likeRequest);
        } catch (LikeNotFoundException exception) {
            Like like = new Like();
            return modelMapper.map(repository.save(like),LikeDTO.class);
        }
    }

    public List<LikeDTO> getAllByUserId(long userId, ClothingListType listType, String typeFilter, String genderFilter, Integer pageNumber) {
        //Convert genderFilter to gender
        Gender gender = null;
        if (genderFilter != null) {
            gender = toGender(genderFilter);
        }

        //Convert typeFilter to list of types
        List<ClothType> typeFilters = null;
        if (typeFilter != null) {
            typeFilters = new ArrayList<>();
            String[] filters = typeFilter.split(",");
            for (String str : filters) {
                typeFilters.add(toClothType(str));
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
            pageRequest = PageRequest.of(pageNumber, AppController.PAGE_SIZE, Sort.Direction.DESC, "id");
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

        return likePage.getContent();
    }

    public LikeDTO findByClothingAndUser(long clothingId, long userId) throws LikeNotFoundException  {
        Optional<Like> like = repository.findByClothingAndUser(clothingId, userId);
        if (like.isEmpty())
            throw new LikeNotFoundException(String.format("No like found with clothingId %d and userId %d", clothingId, userId));

        return modelMapper.map(like, LikeDTO.class);
    }

    public static void sendLike(LikeDTO like) {
        try {
            URL url = new URL("https://ai.peachsconemarket.com/like");
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
                throw new Exception("Did not successfully send like with URL https://ai.peachsconemarket.com/like and data " + jsonObject.toJSONString() + ".");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
