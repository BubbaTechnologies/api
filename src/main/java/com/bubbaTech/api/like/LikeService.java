//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.clothing.ClothType;
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
    public void delete(long clothingId, long userId) {
        if (repository.findByClothingAndUser(clothingId, userId) == null)
            return;
        repository.delete(clothingId,userId);
    }

    @Transactional
    public Like update(Like likeRequest) {
        Like like = repository.findByClothingAndUser(likeRequest.getClothing().getId(), likeRequest.getUser().getId()).orElseThrow(() -> new LikeNotFoundException(likeRequest.getId()));

        like.setId(like.getId());
        like.setClothing(like.getClothing());
        like.setUser(like.getUser());
        like.setRating(likeRequest.getRating());

        return repository.save(like);
    }

    public Like create(Like like) {
        //TODO: Handle likes that are out of bounds
        Optional<Like> foundLike = findByClothingAndUser(like.getClothing().getId(), like.getUser().getId());
        if (foundLike.isPresent()) {
            like.setId(foundLike.get().getId());
            return update(like);
        }

        return repository.save(like);
    }

    public List<Like> getAllByUserId(long userId, int rating, String typeFilter, String genderFilter) {
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

        if (genderFilter != null && typeFilter != null) {
            return repository.findAllByUserIdWithGenderAndTypes(userId, rating, gender, typeFilters);
        } else if (genderFilter != null) {
            return repository.findAllByUserIdWithGender(userId, rating, gender);
        } else if (typeFilter != null) {
            return repository.findAllByUserIdWithTypes(userId, rating, typeFilters);
        }

        return repository.findAllByUserId(userId, rating);
    }

    public Optional<Like> findByClothingAndUser(long clothingId, long userId) {
        return repository.findByClothingAndUser(clothingId, userId);
    }

//    public Like create(Like like, long sessionId) {
//        User user = sessionService.getById(sessionId).getUser();
//        like.setUser(user);
//
//        if (repository.checkLike(like.getUser().getId(), like.getClothing().getId())) {
//            Like foundLike = repository.getLikeByUserAndClothing(like.getUser().getId(), like.getClothing().getId());
//            return this.updateLike(foundLike.getId(), like);
//        }
//
//        return repository.save(like);
//    }
//
//    public List<Like> getAll(long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/like");
//
//        return repository.findAll();
//    }
//
//    public Like update(long id, Like likeRequest, long sessionId) {
//        User user = sessionService.getById(sessionId).getUser();
//        likeRequest.setUser(user);
//
//        return this.updateLike(id, likeRequest);
//    }
//
//    public void delete(long id, long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/like/{id} --DELETE");
//
//        Like like = repository.findById(id).orElseThrow(() -> new LikeNotFoundException(id));
//
//        repository.delete(like);
//    }
//
//    public Like getById(long sessionId, long id) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/like/{id}");
//        return this.getLike(id);
//    }
//
//    public void deleteByItem(long clothingID) {
//        repository.deleteByItem(clothingID);
//    }
//
//    public void deleteByUser(long userID) {
//        repository.deleteByUser(userID);
//    }
//
//    public boolean checkLike(long userId, long clothingId) {
//        return repository.checkLike(userId, clothingId);
//    }
//
//    public Clothing getItem(long id) {
//        return clothingService.getItem(id);
//    }
//
//    private Like getLike(long id) {
//        Optional<Like> result = repository.findById(id);
//
//        if (result.isPresent())
//            return result.get();
//        else
//            throw new LikeNotFoundException(id);
//    }
//
//    private Like updateLike(long id, Like likeRequest) {
//        Like like = repository.findById(id).orElseThrow(() -> new LikeNotFoundException(id));
//
//        like.setId(likeRequest.getId());
//        like.setClothing(likeRequest.getClothing());
//        like.setUser(likeRequest.getUser());
//        like.setRating(likeRequest.getRating());
//
//        return repository.save(like);
//    }
}
