package com.bubbaTech.api.mapping;

import com.bubbaTech.api.clothing.Clothing;
import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.like.Like;
import com.bubbaTech.api.like.LikeDTO;
import com.bubbaTech.api.store.Store;
import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.user.User;
import com.bubbaTech.api.user.UserDTO;
import jakarta.transaction.Transactional;

import java.time.LocalDate;


/**
 * Author: Matthew Groholski
 * Date: 11/03/23
 * Description: A class that holds the mapping methods for entities to DTO. This class is turned into a Bean in Application/Application.config
 */

public class Mapper {

    public UserDTO userToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setGender(user.getGender());
        userDTO.setEnabled(user.getEnabled());
        userDTO.setAccountCreated(user.getAccountCreated());
        userDTO.setLastLogin(user.getLastLogin());
        userDTO.setGrantedAuthorities(user.getGrantedAuthorities());
        userDTO.setDeviceId(user.getDeviceId());

        if (user.getLatitude() != null && user.getLongitude() != null) {
            userDTO.setLatitude(user.getLatitude());
            userDTO.setLongitude(user.getLongitude());
        } else {
            userDTO.setLatitude(0);
            userDTO.setLongitude(0);
        }

        if (user.getBirthDate() == null) {
            userDTO.setBirthDate(LocalDate.EPOCH);
        } else {
            userDTO.setBirthDate(user.getBirthDate());
        }

        if (user.getDeviceId() == null) {
            userDTO.setDeviceId("");
        } else {
            userDTO.setDeviceId(user.getDeviceId());
        }

        return userDTO;
    }


    public User userDTOToUser(UserDTO userDTO) {
        User user = new User();

        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setGender(userDTO.getGender());
        user.setEnabled(userDTO.getEnabled());
        user.setAccountCreated(userDTO.getAccountCreated());
        user.setLastLogin(userDTO.getLastLogin());
        user.setGrantedAuthorities(userDTO.getGrantedAuthorities());
        user.setDeviceId(userDTO.getDeviceId());
        user.setLatitude(userDTO.getLatitude());
        user.setLongitude(userDTO.getLongitude());

        return user;
    }


    public StoreDTO storeToStoreDTO(Store store) {
        StoreDTO storeDTO = new StoreDTO();

        storeDTO.setId(store.getId());
        storeDTO.setName(store.getName());
        storeDTO.setURL(store.getURL());
        storeDTO.setEnabled(store.isEnabled());

        return storeDTO;
    }


    public Store storeDTOToStore(StoreDTO storeDTO) {
        Store store = new Store();

        store.setId(storeDTO.getId());
        store.setName(storeDTO.getName());
        store.setURL(storeDTO.getURL());
        store.setEnabled(storeDTO.isEnabled());

        return store;
    }

    @Transactional
    public ClothingDTO clothingToClothingDTO(Clothing clothing) {
        ClothingDTO clothingDTO = new ClothingDTO();

        clothingDTO.setId(clothing.getId());
        clothingDTO.setName(clothing.getName());
        //Forces imageUrl to load
        clothing.getImageURL().size();
        clothingDTO.setImageURL(clothing.getImageURL());
        clothingDTO.setProductURL(clothing.getProductURL());
        clothingDTO.setStore(this.storeToStoreDTO(clothing.getStore()));
        clothingDTO.setType(clothing.getClothingType());
        clothingDTO.setGender(clothing.getGender());
        clothingDTO.setDate(clothing.getDate());
        //Forces tags to load
        clothing.getTags().size();
        clothingDTO.setTags(clothing.getTags());

        return clothingDTO;
    }


    public Clothing clothingDTOToClothing(ClothingDTO clothingDTO) {
        Clothing clothing = new Clothing();

        clothing.setId(clothingDTO.getId());
        clothing.setName(clothingDTO.getName());
        clothing.setImageURL(clothingDTO.getImageURL());
        clothing.setProductURL(clothingDTO.getProductURL());
        clothing.setStore(this.storeDTOToStore(clothingDTO.getStore()));
        clothing.setClothingType(clothingDTO.getType());
        clothing.setGender(clothingDTO.getGender());
        clothing.setDate(clothingDTO.getDate());
        clothing.setTags(clothingDTO.getTags());

        return clothing;
    }


    public LikeDTO likeToLikeDTO(Like like) {
        LikeDTO likeDTO = new LikeDTO();

        likeDTO.setId(like.getId());
        likeDTO.setUser(this.userToUserDTO(like.getUser()));
        likeDTO.setClothing(this.clothingToClothingDTO(like.getClothing()));
        likeDTO.setRating(like.getRating());
        likeDTO.setDate(like.getDate());
        likeDTO.setLiked(like.isLiked());
        likeDTO.setBought(like.isBought());

        return likeDTO;
    }


    public  Like likeDTOToLike(LikeDTO likeDTO) {
        Like like = new Like();

        like.setId(likeDTO.getId());
        like.setUser(this.userDTOToUser(likeDTO.getUser()));
        like.setClothing(this.clothingDTOToClothing(likeDTO.getClothing()));
        like.setRating(likeDTO.getRating());
        like.setDate(likeDTO.getDate());
        like.setLiked(likeDTO.isLiked());
        like.setBought(likeDTO.isBought());

        return like;
    }
}
