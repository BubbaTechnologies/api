//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.app.AppController;
import com.bubbaTech.api.data.storeStatDTO;
import com.bubbaTech.api.info.ServiceLogger;
import com.bubbaTech.api.like.LikeService;
import com.bubbaTech.api.store.Store;
import com.bubbaTech.api.store.StoreService;
import com.bubbaTech.api.user.Gender;
import com.bubbaTech.api.user.UserService;
import lombok.AllArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ClothingService {
    private final static int MAX_RANDS = 100;
    private final static int WEEKS_AGO = 3;

    private final static double randomClothingChance = 0.4;
    private final ClothingRepository repository;
    private final LikeService likeService;
    private final UserService userService;
    private final StoreService storeService;

    private final ServiceLogger logger;


    public Optional<Clothing> getById(long clothingId) {
        return repository.findById(clothingId);
    }

    public List<Clothing> recommendClothingIdList(long userId, String typeFilter, String genderFilter) {
        Gender gender = genderStringToEnum(userId, genderFilter);
        List<ClothType> typeFilters = typeStringToList(typeFilter);
        try {
            String baseUrl = "https://ai.peachsconemarket.com/recommendationList";
            StringBuilder query = new StringBuilder("userId=" + URLEncoder.encode(Long.toString(userId), StandardCharsets.UTF_8) +
                    "&gender=" + URLEncoder.encode(gender.toString(), StandardCharsets.UTF_8));
            if (typeFilters != null) {
                query.append("&clothingType=");
                for (ClothType type : typeFilters) {
                    query.append(URLEncoder.encode(type.toString(), StandardCharsets.UTF_8)).append(",");
                }
                query.deleteCharAt(query.length() - 1);
            }
            URL url = new URL(baseUrl + "?" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            List<Clothing> items = new ArrayList<>();
            if (responseCode == 200) {
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = inputStream.readLine()) != null) {
                    responseBuilder.append(line);
                }

                inputStream.close();
                JSONObject jsonResponse = (JSONObject) new JSONParser().parse(responseBuilder.toString());
                JSONArray clothingIdArray = (JSONArray) jsonResponse.get("clothingItems");

                for (Object id : clothingIdArray) {
                    double choice = this.randomDouble();
                    //Introduces randomness between recommended clothing
                    if (choice <= randomClothingChance)
                        items.add(getRandom(userId, typeFilters, gender));
                    Optional<Clothing> item = repository.getById((long) id);
                    if (item.isPresent() && items.size() < AppController.CLOTHING_COUNT && !likeCheck(item.get(), userId))
                        items.add(item.get());
                }
            }
            //Fill rest of items with random
            for (int i = items.size(); i < AppController.CLOTHING_COUNT; i++) {
                items.add(getRandom(userId, typeFilters, gender));
            }
            return items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Clothing getCard(long userId, String typeFilter, String genderFilter) {
        Gender gender = genderStringToEnum(userId, genderFilter);
        List<ClothType> typeFilters = typeStringToList(typeFilter);

        double choice = this.randomDouble();
        if (choice <= randomClothingChance) {
            return getRandom(userId, typeFilters, gender);
        } else {
            try {
                String baseUrl = "https://ai.peachsconemarket.com/recommendation";
                StringBuilder query = new StringBuilder("userId=" + URLEncoder.encode(Long.toString(userId), StandardCharsets.UTF_8) +
                        "&gender=" + URLEncoder.encode(gender.toString(), StandardCharsets.UTF_8));
                if (typeFilters != null) {
                    query.append("&clothingType=");
                    for (ClothType type : typeFilters) {
                        query.append(URLEncoder.encode(type.toString(), StandardCharsets.UTF_8)).append(",");
                    }
                    query.deleteCharAt(query.length() - 1);
                }
                URL url = new URL(baseUrl + "?" + query);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = inputStream.readLine()) != null) {
                        responseBuilder.append(line);
                    }

                    inputStream.close();
                    JSONObject jsonResponse = (JSONObject) new JSONParser().parse(responseBuilder.toString());
                    long clothingId = (long) jsonResponse.get("clothingId");
                    Optional<Clothing> item = repository.getById(clothingId);
                    if (item.isPresent() && !likeCheck(item.get(), userId))
                        return item.get();
                    else
                        logger.error("Invalid Clothing Id: " + clothingId + ".");
                } else{
                    logger.error("Error getting recommendation with following url " + baseUrl + "?" + query + " . Received response code " + responseCode + ".");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getRandom(userId, typeFilters, gender);
    }

    private double randomDouble() {
        return (Math.random()) + (double) 0;
    }

    private Clothing getRandom(long userId, List<ClothType> typeFilters, Gender gender) {
        long count = repository.count();
        if (count == 0)
            throw new RuntimeException("Empty Repository");

        int loops = 0;
        while (loops < MAX_RANDS) {
            loops++;

            Page<Clothing> clothingPage;
            LocalDate randomDateRestriction = LocalDate.now().minusWeeks(WEEKS_AGO);
            if (typeFilters == null) {
                long amount = repository.countByGender(gender, randomDateRestriction);
                int index = (int) (Math.random() * amount);
                clothingPage = repository.findAllWithGender(gender, PageRequest.of(index, 1), randomDateRestriction);
            } else {
                long amount = repository.countByGenderAndTypes(gender, typeFilters, randomDateRestriction);
                int index = (int) (Math.random() * amount);
                clothingPage = repository.findAllWithGenderAndTypes(gender, typeFilters, PageRequest.of(index, 1), randomDateRestriction);
            }

            Clothing item;
            if (clothingPage.hasContent())
                item = clothingPage.getContent().get(0);
            else
                continue;

            if (likeCheck(item, userId))
                continue;

            return item;
        }
        return null;
    }

    private Boolean likeCheck(Clothing item, long userId) {
        return likeService.findByClothingAndUser(item.getId(), userId).isPresent();
    }

    @Transactional
    public Clothing update(Clothing item) {
        Optional<Clothing> optionalClothingItem = this.findByUrl(item.getProductURL());
        if (optionalClothingItem.isPresent()) {
            Clothing clothingItem = optionalClothingItem.get();
            clothingItem.setGender(item.getGender());
            clothingItem.setName(item.getName());
            clothingItem.setImageURL(item.getImageURL());
            return repository.save(clothingItem);
        }
        return null;
    }

    public Clothing create(Clothing item) {
        Optional<Clothing> optionalClothingItem = this.findByUrl(item.getProductURL());
        if (optionalClothingItem.isPresent()) {
            return update(optionalClothingItem.get());
        } else {
            return repository.save(item);
        }
    }

    public Optional<Clothing> findByUrl(String url) {
        return repository.findByProductUrl(url);
    }

    public List<storeStatDTO> getClothingPerStoreData() {
        List<Store> stores = storeService.getAll();
        List<storeStatDTO> storeStats = new ArrayList<>();
        for (Store store: stores) {
            Long maleCount = repository.countByStoreAndGender(store, Gender.MALE);
            Long femaleCount = repository.countByStoreAndGender(store, Gender.FEMALE);
            Long boyCount = repository.countByStoreAndGender(store, Gender.BOY);
            Long girlCount = repository.countByStoreAndGender(store, Gender.GIRL);
            Long kidCount = repository.countByStoreAndGender(store, Gender.KID);
            Long unisexCount = repository.countByStoreAndGender(store, Gender.UNISEX);
            Long otherCount = repository.countByStoreAndType(store, ClothType.OTHER);
            storeStats.add(new storeStatDTO(store.getName(),maleCount, femaleCount, boyCount, girlCount, kidCount, unisexCount, otherCount));
        }
        return storeStats;
    }

    private List<ClothType> typeStringToList(String typeFilter) {
        List<ClothType> typeFilters = null;

        if (typeFilter != null) {
            typeFilters = new ArrayList<>();
            String[] typeFiltersString = typeFilter.split(",");
            for (String str : typeFiltersString)
                typeFilters.add(toClothType(str));
        }
        return typeFilters;
    }

    private Gender genderStringToEnum(long userId, String genderFilter) {
        Gender gender;
        if (genderFilter == null)
            gender = userService.getGenderById(userId);
        else
            gender = toGender(genderFilter);
        return gender;
    }

    static public Gender toGender(String gender) {
        return switch (gender.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            case "boy" -> Gender.BOY;
            case "girl" -> Gender.GIRL;
            case "kids" -> Gender.KID;
            default -> Gender.UNISEX;
        };
    }

    static public ClothType toClothType(String type) {
        return switch (type.toLowerCase()) {
            case "top" -> ClothType.TOP;
            case "bottom" -> ClothType.BOTTOM;
            case "shoes" -> ClothType.SHOES;
            case "underclothing" -> ClothType.UNDERCLOTHING;
            case "jacket" -> ClothType.JACKET;
            case "skirt" -> ClothType.SKIRT;
            case "one_piece" -> ClothType.ONE_PIECE;
            case "sleepwear" -> ClothType.SLEEPWEAR;
            case "dress" -> ClothType.DRESS;
            case "accessory" -> ClothType.ACCESSORY;
            case "set" -> ClothType.SET;
            default -> ClothType.OTHER;
        };
    }
}


