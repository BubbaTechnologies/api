//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.medianGroup;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MedianGroupRepository extends JpaRepository<MedianGroup, Long> {
//    @Query("SELECT g FROM MedianGroup g WHERE ?1 IN g.users")
//    MedianGroup getWithUser(long userId);
//
//    @Query("SELECT g FROM MedianGroup g WHERE ?1 IN g.items")
//    List<MedianGroup> getAllWithItem(long itemId);
}
