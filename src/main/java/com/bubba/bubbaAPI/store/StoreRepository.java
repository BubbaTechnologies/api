//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022
package com.bubba.bubbaAPI.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    @Query("SELECT s FROM Store s WHERE s.URL = ?1")
    Optional<Store> findByUrl(String url);

    @Query("SELECT s FROM Store s WHERE s.name = ?1")
    Optional<Store> findByName(String name);
}
