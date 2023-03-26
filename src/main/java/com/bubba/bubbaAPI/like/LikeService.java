//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.like;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class LikeService {
    private final LikeRepository repository;


    public LikeService(LikeRepository repository) {
        super();
        this.repository = repository;
    }

    public void delete(long clothingId, long userId) {
        if (repository.findByClothingAndUser(clothingId, userId) == null)
            return;
        repository.delete(clothingId,userId);
    }

    public Like update(Like likeRequest) {
        Like like = repository.findById(likeRequest.getId()).orElseThrow(() -> new LikeNotFoundException(likeRequest.getId()));

        like.setId(likeRequest.getId());
        like.setClothing(likeRequest.getClothing());
        like.setUser(likeRequest.getUser());
        like.setRating(likeRequest.getRating());

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

    public List<Like> getAllByUserId(long userId, int rating) {
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
