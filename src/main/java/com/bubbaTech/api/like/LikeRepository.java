//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.ClothingTag;
import com.bubbaTech.api.user.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.store.enabled = true")
    Long countByUserId(long userId, boolean liked, boolean bought);
    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.store.enabled = true")
    Page<Like> findAllByUserId(long userId, boolean liked, boolean bought, Pageable pageable);
    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.clothingType IN ?4 AND l.clothing.store.enabled = true")
    Page<Like> findAllByUserIdWithTypes(long userId, boolean liked, boolean bought, List<ClothType> type, Pageable pageable);
    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.gender = ?4 AND l.clothing.store.enabled = true")
    Page<Like> findAllByUserIdWithGender(long userId, boolean liked, boolean bought, Gender gender, Pageable pageable);
    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.gender = ?4 AND l.clothing.clothingType" +
            " IN ?5 AND l.clothing.store.enabled = true")
    Page<Like> findAllByUserIdWithGenderAndTypes(long userId, boolean liked, boolean bought, Gender gender, List<ClothType> type, Pageable pageable);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.gender = ?4 AND l.clothing.clothingType" +
            " IN ?5 AND l.clothing.store.enabled = true")
    Long countAllByUserIdWithGenderAndTypes(long userId, boolean liked, boolean bought, Gender gender, List<ClothType> type);
    @Query("SELECT count(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.store.enabled = true")
    Long countAllByUserId(long userId, boolean liked, boolean bought);
    @Query("SELECT count(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.clothingType IN ?4 AND l.clothing.store.enabled = true")
    Long countAllByUserIdWithTypes(long userId, boolean liked, boolean bought, List<ClothType> type);
    @Query("SELECT count(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.gender = ?4 AND l.clothing.store.enabled = true")
    Long countAllByUserIdWithGender(long userId, boolean liked, boolean bought, Gender gender);
    @Query("SELECT DISTINCT l.clothing.gender FROM Like l WHERE l.liked = true AND l.user.id = ?1")
    List<Gender> getAllUniqueGenders(long userId);
    @Query("SELECT DISTINCT l.clothing.clothingType FROM Like l WHERE l.liked = true AND l.user.id = ?1 AND l.clothing.gender = ?2")
    List<ClothType> getAllUniqueTypesByGender(long userId, Gender gender);
    @Query("SELECT DISTINCT l.clothing.tags FROM Like l WHERE l.liked = true AND l.user.id = ?1 AND l.clothing.gender = ?2 AND l.clothing.clothingType = ?3")
    List<ClothingTag> getAllUniqueTagsByTypeAndGender(long userId, Gender gender, ClothType type);
}
