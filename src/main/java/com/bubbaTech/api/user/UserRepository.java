//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = ?1")
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.lastLogin >= :beforeDate")
    List<User> activeUsersInLastWeek(LocalDate beforeDate);

    @Query("SELECT u FROM User u WHERE u.accountCreated >= :beforeDate")
    List<User> lastDaySignUps(LocalDate beforeDate);

//    @Query("DELETE FROM Like l WHERE l.user.id = ?1")
//    @Modifying
//    void deleteAssociatedLikes(long userId);
//
//    @Query("SELECT u.gender FROM User u WHERE u.id = ?1")
//    Gender getGenderById(Long userId);
//
//    @Query("SELECT u FROM User u WHERE u.email = ?1")
//    User getByEmail(String email);
//
//    @Query("SELECT u FROM User u WHERE u.username = ?1")
//    User getByUsername(String username);
//
//    @Query("SELECT case WHEN count(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = ?1 AND u.password = ?2")
//    boolean checkByEmail(String email, String password);
//
//    @Query("SELECT case WHEN count(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = ?1 AND u.password = ?2")
//    boolean checkByUsername(String username, String password);
//
//    @Query("SELECT case WHEN count(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = ?1")
//    boolean usernameExists(String email);
//
//    @Query("SELECT case WHEN count(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = ?1")
//    boolean emailExists(String email);
}