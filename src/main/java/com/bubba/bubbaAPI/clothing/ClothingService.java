//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.clothing;

import com.bubba.bubbaAPI.like.LikeService;
import com.bubba.bubbaAPI.user.Gender;
import com.bubba.bubbaAPI.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ClothingService {
    private final static int MAX_RANDS = 500;

    private final static double randomClothingChance = 1;
    private final ClothingRepository repository;
    private final LikeService likeService;
    private final UserService userService;


    public Optional<Clothing> getById(long clothingId) {
        return repository.findById(clothingId);
    }

    //TODO: Optimize
    public Clothing getCard(long userId, String typeFilter, String genderFilter) {
        Gender gender;
        ClothType type = null;

        if (genderFilter == null)
            gender = userService.getGenderById(userId);
        else
            gender = toGender(genderFilter);

        if (typeFilter != null)
            type = toClothType(typeFilter);

        double choice = this.randomDouble(0, 1);
        if (choice <= randomClothingChance) {
            long count = repository.count();
            if (count == 0)
                throw new RuntimeException("Empty Repository");

            int loops = 0;
            long randLong = randomLong(1L, count);
            while (loops < MAX_RANDS) {
                loops++;
                Optional<Clothing> optionalItem = repository.getById(randLong);
                Clothing item;
                if (optionalItem.isPresent())
                    item = optionalItem.get();
                else {
                    randLong = randomLong(1L, count);
                    continue;
                }

                Collection<Gender> genders = item.getGender();
                boolean genderCheck = false;
                for (Gender g : genders) {
                    if (g.getIntValue() == gender.getIntValue()) {
                        genderCheck = true;
                        break;
                    }
                }

                boolean likeCheck = likeService.findByClothingAndUser(randLong, userId).isEmpty();

                boolean typeCheck;
                if (type == null && item.getType() != ClothType.OTHER)
                    typeCheck = true;
                else
                    typeCheck = type.getIntValue() == item.getType().getIntValue();

                if (!(genderCheck && typeCheck && likeCheck)) {
                    randLong = randomLong(1L, count);
                    continue;
                }

                return item;
            }

        } //TODO: Create return by group


        return null;
    }

    private Long randomLong(Long start, Long end) {
        return start + (long) (Math.random() * (end - start + 1));
    }

    private double randomDouble(double start, double end) {
        return (Math.random() * (end - start)) + start;
    }

    public Clothing create(Clothing item) {
        return repository.save(item);
    }

    public Optional<Clothing> findByUrl(String url) {
        return repository.findByProductUrl(url);
    }

    private Gender toGender(String gender) {
        switch (gender) {
            case "male":
                return Gender.MALE;
            case "female":
                return Gender.FEMALE;
            case "boy":
                return Gender.BOY;
            case "girl":
                return Gender.GIRL;
            case "kids":
                return Gender.KID;
            default:
                return Gender.UNISEX;
        }
    }

    private ClothType toClothType(String type) {
        switch (type) {
            case "top":
                return ClothType.TOP;
            case "bottom":
                return ClothType.BOTTOM;
            case "shoes":
                return ClothType.SHOES;
            case "underclothing":
                return ClothType.UNDERCLOTHING;
            case "jacket":
                return ClothType.JACKET;
            case "skirt":
                return ClothType.SKIRT;
            case "one piece":
                return ClothType.ONE_PIECE;
            case "accessory":
                return ClothType.ACCESSORY;
            default:
                return ClothType.OTHER;
        }
    }


//
//    public Clothing update(long itemId, long sessionId, Clothing clothingRequest) {
//
//        return updateItem(itemId, clothingRequest);
//
//    }
//
//    public void delete(long itemId, long sessionId) {
//
//        Clothing item = repository.findById(itemId).orElseThrow(() -> new ClothingNotFoundException(itemId));
//
//        repository.delete(item);
//        groupService.removeItem(item.getId());
//        likeService.deleteByItem(itemId);
//    }
//
//    public Clothing getById(long itemId, long sessionId) {
//
//        return this.getItem(itemId);
//    }
//
//
//    public List<Clothing> getRecommended(long sessionId) {
//        //TODO
//        throw new NotImplementedException("getRecommended");
//    }
//
//
//
//    public void deleteByStore(Store store) {
//        repository.deleteByStore(store.getId());
//    }
//
//    public Store getStoreById(long storeId) {
//        return storeService.getStore(storeId);
//    }
//
//    Clothing getItem(long id) {
//        Optional<Clothing> result = repository.findById(id);
//
//        if (result.isPresent()) {
//            return result.get();
//        } else {
//            throw new ClothingNotFoundException(id);
//        }
//    }
//
//
//    private Clothing updateItem(long itemId, Clothing clothingRequest) {
//        Clothing item = repository.findById(itemId).orElseThrow(() -> new ClothingNotFoundException(itemId));
//
//        item.setType(clothingRequest.getType());
//        item.setProductURL(clothingRequest.getProductURL());
//        item.setImageURL(clothingRequest.getImageURL());
//        item.setId(clothingRequest.getId());
//        item.setGender(clothingRequest.getGender());
//        item.setName(clothingRequest.getName());
//        item.setStore(clothingRequest.getStore());
//
//        return repository.save(item);
//    }
}

