//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.user.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClothingRepository extends JpaRepository<Clothing, Long> {

    @Query("SELECT c FROM Clothing c WHERE c.productURL = ?1")
    Optional<Clothing> findByProductUrl(String url);

    @Query("SELECT c FROM Clothing c WHERE c.id = ?1 AND c.store.enabled = true")
    Optional<Clothing> getById(long id);
    @Query("SELECT COUNT(c) FROM Clothing c WHERE c.gender=?1 AND NOT c.clothingType = 10")
    long countByGender(Gender gender);

    @Query("SELECT COUNT(c) FROM Clothing c WHERE c.gender = ?1 AND c.clothingType IN ?2")
    long countByGenderAndTypes(Gender gender, List<ClothType> type);
    @Query("SELECT c FROM Clothing c WHERE c.gender = ?1 AND NOT c.clothingType = 10 ")
    Page<Clothing> findAllWithGender(Gender gender, Pageable pageable);

    @Query("SELECT c FROM Clothing c WHERE c.gender = ?1 AND c.clothingType IN ?2")
    Page<Clothing> findAllWithGenderAndTypes(Gender gender, List<ClothType> type, Pageable pageable);
}
