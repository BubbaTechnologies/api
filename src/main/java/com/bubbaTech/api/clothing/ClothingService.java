//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.like.LikeService;
import com.bubbaTech.api.user.Gender;
import com.bubbaTech.api.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ClothingService {
    private final static int MAX_RANDS = 100;

    private final static double randomClothingChance = 1;
    private final ClothingRepository repository;
    private final LikeService likeService;
    private final UserService userService;


    public Optional<Clothing> getById(long clothingId) {
        return repository.findById(clothingId);
    }

    public Clothing getCard(long userId, String typeFilter, String genderFilter) {
        //TODO
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
            while (loops < MAX_RANDS){
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

                boolean typeCheck = false;
                if (type == null && item.getType() != ClothType.OTHER)
                    typeCheck = true;
                else if (type != null) {
                    typeCheck = type.getIntValue() == item.getType().getIntValue();
                }

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
        Optional<Clothing> optionalClothingItem = this.findByUrl(item.getProductURL());
        if (optionalClothingItem.isPresent()) {
            Clothing clothingItem = optionalClothingItem.get();
            clothingItem.setGender(item.getGender());
            clothingItem.setName(item.getName());
            clothingItem.setImageURL(item.getImageURL());
            return repository.save(clothingItem);

        } else {
            return repository.save(item);
        }
    }

    public Optional<Clothing> findByUrl(String url) {
        return repository.findByProductUrl(url);
    }

    private Gender toGender(String gender) {
        return switch (gender) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            case "boy" -> Gender.BOY;
            case "girl" -> Gender.GIRL;
            case "kids" -> Gender.KID;
            default -> Gender.UNISEX;
        };
    }

    private ClothType toClothType(String type) {
        return switch (type) {
            case "top" -> ClothType.TOP;
            case "bottom" -> ClothType.BOTTOM;
            case "shoes" -> ClothType.SHOES;
            case "underclothing" -> ClothType.UNDERCLOTHING;
            case "jacket" -> ClothType.JACKET;
            case "skirt" -> ClothType.SKIRT;
            case "one_piece" -> ClothType.ONE_PIECE;
            case "dress" -> ClothType.DRESS;
            case "accessory" -> ClothType.ACCESSORY;
            default -> ClothType.OTHER;
        };
    }
}


