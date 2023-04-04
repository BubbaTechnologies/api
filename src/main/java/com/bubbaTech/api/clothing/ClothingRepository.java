//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClothingRepository extends JpaRepository<Clothing, Long> {

    @Query("SELECT c FROM Clothing c WHERE c.productURL = ?1")
    Optional<Clothing> findByProductUrl(String url);

    @Query("SELECT c FROM Clothing c WHERE c.id = ?1 AND c.store.enabled = true")
    Optional<Clothing> getById(long id);
}
