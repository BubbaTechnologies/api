//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.ClothingListType;
import com.bubbaTech.api.user.Gender;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.bubbaTech.api.clothing.ClothingService.toClothType;
import static com.bubbaTech.api.clothing.ClothingService.toGender;

@org.springframework.stereotype.Service
public class LikeService {
    private final LikeRepository repository;

    public LikeService(LikeRepository repository) {
        super();
        this.repository = repository;
    }

    @Transactional
    public Like update(Like likeRequest) {
        Like like = repository.findByClothingAndUser(likeRequest.getClothing().getId(), likeRequest.getUser().getId()).orElseThrow(() -> new LikeNotFoundException(likeRequest.getId()));

        like.setBought(likeRequest.isBought());
        like.setLiked(likeRequest.isLiked());
        like.setRating(likeRequest.getRating() + like.getRating());

        return repository.save(like);
    }

    public Like create(Like like) {
        Optional<Like> foundLike = findByClothingAndUser(like.getClothing().getId(), like.getUser().getId());
        if (foundLike.isPresent()) {
            like.setId(foundLike.get().getId());
            return update(like);
        }

        return repository.save(like);
    }

    public List<Like> getAllByUserId(long userId, ClothingListType listType, String typeFilter, String genderFilter) {
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
            case LIKE -> {
                liked = true;
            }
            case BOUGHT -> {
                liked = true;
                bought = true;
            }
        }

        if (genderFilter != null && typeFilter != null) {
            return repository.findAllByUserIdWithGenderAndTypes(userId, liked, bought, gender, typeFilters);
        } else if (genderFilter != null) {
            return repository.findAllByUserIdWithGender(userId, liked, bought, gender);
        } else if (typeFilter != null) {
            return repository.findAllByUserIdWithTypes(userId, liked, bought, typeFilters);
        }

        return repository.findAllByUserId(userId, liked, bought);
    }

    public Optional<Like> findByClothingAndUser(long clothingId, long userId) {
        return repository.findByClothingAndUser(clothingId, userId);
    }
}
