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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query("SELECT l FROM Like l WHERE l.clothing.id = ?1 AND l.user.id = ?2")
    Optional<Like> findByClothingAndUser(long clothingId, long userId);
    @Query("DELETE FROM Like l WHERE l.user.id = ?2 AND l.clothing.id = ?1")
    @Modifying
    void delete(long clothingId, long userId);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Long countByUserId(long userId, boolean liked, boolean bought);
    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Page<Like> findAllByUserId(long userId, boolean liked, boolean bought, Pageable pageable);
    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.clothingType IN ?4 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Page<Like> findAllByUserIdWithTypes(long userId, boolean liked, boolean bought, List<ClothType> type, Pageable pageable);
    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.gender = ?4 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Page<Like> findAllByUserIdWithGender(long userId, boolean liked, boolean bought, Gender gender, Pageable pageable);
    @Query("SELECT l FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.gender = ?4 AND l.clothing.clothingType" +
            " IN ?5 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Page<Like> findAllByUserIdWithGenderAndTypes(long userId, boolean liked, boolean bought, Gender gender, List<ClothType> type, Pageable pageable);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.gender = ?4 AND l.clothing.clothingType" +
            " IN ?5 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Long countAllByUserIdWithGenderAndTypes(long userId, boolean liked, boolean bought, Gender gender, List<ClothType> type);
    @Query("SELECT count(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Long countAllByUserId(long userId, boolean liked, boolean bought);
    @Query("SELECT count(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.clothingType IN ?4 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Long countAllByUserIdWithTypes(long userId, boolean liked, boolean bought, List<ClothType> type);
    @Query("SELECT count(l) FROM Like l WHERE l.user.id = ?1 AND l.liked = ?2 AND l.bought = ?3 AND l.clothing.gender = ?4 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Long countAllByUserIdWithGender(long userId, boolean liked, boolean bought, Gender gender);
    @Query("SELECT DISTINCT l.clothing.gender FROM Like l WHERE l.liked = true AND l.user.id = ?1 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    List<Gender> getAllUniqueGenders(long userId);
    @Query("SELECT DISTINCT l.clothing.clothingType FROM Like l WHERE l.liked = true AND l.user.id = ?1 AND l.clothing.gender = ?2 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    List<ClothType> getAllUniqueTypesByGender(long userId, Gender gender);
    @Query("SELECT DISTINCT l.clothing.tags FROM Like l WHERE l.liked = true AND l.user.id = ?1 AND l.clothing.gender = ?2 AND l.clothing.clothingType = ?3 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    List<ClothingTag> getAllUniqueTagsByTypeAndGender(long userId, Gender gender, ClothType type);
    @Query("SELECT l FROM Like l WHERE l.user.id IN :userIds AND l.date <= :date " +
            "AND l.liked = true AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Page<Like> getActivity(List<Long> userIds, LocalDateTime date, Pageable pageable);
    @Query("SELECT l FROM Like l WHERE l.user.id IN :userIds AND l.clothing.gender = :gender AND l.date <= :date " +
            "AND l.liked = true AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Page<Like> getActivityByGender(List<Long> userIds, Gender gender, LocalDateTime date, Pageable pageable);

    @Query("SELECT l FROM Like l WHERE l.user.id IN :userIds AND l.clothing.clothingType IN :types AND l.date <= :date " +
            "AND l.liked = true AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Page<Like> getActivityByType(List<Long> userIds, List<ClothType> types, LocalDateTime date, Pageable pageable);
    @Query("SELECT l  FROM Like l WHERE l.user.id IN :userIds AND l.clothing.gender = :gender AND l.clothing.clothingType IN :types AND l.date <= :date " +
            "AND l.liked = true AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    Page<Like> getActivityByTypeAndGender(List<Long> userIds, Gender gender, List<ClothType> types, LocalDateTime date, Pageable pageable);

    @Query("SELECT DISTINCT l.clothing.gender FROM Like l WHERE l.liked = true AND l.user.id IN ?1 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    List<Gender> getAllUniqueGendersForUserIds(List<Long> userId);
    @Query("SELECT DISTINCT l.clothing.clothingType FROM Like l WHERE l.liked = true AND l.user.id IN ?1 AND l.clothing.gender = ?2 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    List<ClothType> getAllUniqueTypesForUserIdsByGender(List<Long> userIds, Gender gender);
    @Query("SELECT DISTINCT l.clothing.tags FROM Like l WHERE l.liked = true AND l.user.id IN ?1 AND l.clothing.gender = ?2 AND l.clothing.clothingType = ?3 AND l.clothing.store.enabled = true AND l.clothing.enabled = true")
    List<ClothingTag> getAllUniqueTagsForUserIdsByTypeAndGender(List<Long> userIds, Gender gender, ClothType type);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id IN ?1 AND l.liked = true AND l.clothing.gender = ?2 AND l.clothing.clothingType" +
            " IN ?3 AND l.clothing.store.enabled = true AND l.clothing.enabled = true AND l.date <= ?4")
    Long countAllByUserIdsWithGenderAndTypes(List<Long> userIds, Gender gender, List<ClothType> type, LocalDateTime date);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id IN ?1 AND l.liked = true AND l.clothing.store.enabled = true AND l.clothing.enabled = true AND l.date <= ?2")
    Long countAllByUserIds(List<Long> userIds, LocalDateTime date);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id IN ?1 AND l.liked = true AND l.clothing.clothingType IN ?2 AND l.clothing.store.enabled = true AND l.clothing.enabled = true AND l.date <= ?3")
    Long countAllByUserIdsWithTypes(List<Long> userIds, List<ClothType> type, LocalDateTime date);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id IN ?1 AND l.liked = true AND l.clothing.gender = ?2 AND l.clothing.store.enabled = true AND l.clothing.enabled = true AND l.date <= ?3")
    Long countAllByUserIdsWithGender(List<Long> userIds, Gender gender, LocalDateTime date);
}
