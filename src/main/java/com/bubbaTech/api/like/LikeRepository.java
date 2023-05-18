//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.user.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query("SELECT l FROM Like l WHERE l.clothing.id = ?1 AND l.user.id = ?2")
    Optional<Like> findByClothingAndUser(long clothingId, long userId);

    @Query("DELETE FROM Like l WHERE l.user.id = ?2 AND l.clothing.id = ?1")
    @Modifying
    void delete(long clothingId, long userId);

    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.rating >= ?2")
    List<Like> findAllByUserId(long userId, int rating);

    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.rating >= ?2 AND l.clothing.clothingType = ?3")
    List<Like> findAllByUserIdWithType(long userId, int rating, ClothType type);

    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.rating >= ?2 AND ?3 IN l.clothing.gender")
    List<Like> findAllByUserIdWithGender(long userId, int rating, Gender gender);

    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.rating >= ?2 AND ?3 IN l.clothing.gender AND l.clothing.clothingType" +
            " = ?4")
    List<Like> findAllByUserIdWithGenderAndType(long userId, int rating, Gender gender, ClothType type);

//    @Query("SELECT l FROM Like l WHERE l.clothing.id = ?1")
//    List<Like> findByItem(long clothingID);
//
//    @Query("DELETE FROM Like l WHERE l.clothing.id = ?1")
//    void deleteByItem(long clothingID);
//
//    @Query("DELETE FROM Like l WHERE l.user.id = ?1")
//    void deleteByUser(long userID);
//
//    @Query("SELECT case WHEN count(l) > 0 THEN true ELSE false END FROM Like l WHERE l.clothing.id = ?2 AND l.user.id = ?1")
//    boolean checkLike(long userId, long clothingId);
//
//    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.clothing.id = ?2")
//    Like getLikeByUserAndClothing(long userId, long clothingId);
}
