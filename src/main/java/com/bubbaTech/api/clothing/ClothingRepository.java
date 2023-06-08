//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.store.Store;
import com.bubbaTech.api.user.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClothingRepository extends JpaRepository<Clothing, Long> {

    @Query("SELECT c FROM Clothing c WHERE c.productURL = :url")
    Optional<Clothing> findByProductUrl(String url);
    @Query("SELECT c FROM Clothing c WHERE c.id = :id AND c.store.enabled = true")
    Optional<Clothing> getById(long id);
    @Query("SELECT COUNT(c) FROM Clothing c WHERE c.gender=:gender AND NOT c.clothingType = 10 AND c.store.enabled = true AND c.date >= :date")
    long countByGender(Gender gender, LocalDate date);
    @Query("SELECT c FROM Clothing c WHERE c.gender = :gender AND NOT c.clothingType = 10 AND c.store.enabled = true AND c.date >= :date")
    Page<Clothing> findAllWithGender(Gender gender, Pageable pageable, LocalDate date);
    @Query("SELECT COUNT(c) FROM Clothing c WHERE c.gender = :gender AND c.clothingType IN :type AND c.store.enabled = true AND c.date >= :date")
    long countByGenderAndTypes(Gender gender, List<ClothType> type, LocalDate date);
    @Query("SELECT c FROM Clothing c WHERE c.gender = :gender AND c.clothingType IN :type AND c.store.enabled = true AND c.date >= :date")
    Page<Clothing> findAllWithGenderAndTypes(Gender gender, List<ClothType> type, Pageable pageable, LocalDate date);
    @Query("SELECT COUNT(c) FROM Clothing c WHERE c.store = :store AND c.gender = :gender")
    long countByStoreAndGender(Store store, Gender gender);
    @Query("SELECT COUNT(c) FROM Clothing c WHERE c.store = :store AND c.clothingType = :type")
    long countByStoreAndType(Store store, ClothType type);
}
