//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.data.storeStatDTO;
import com.bubbaTech.api.info.ServiceLogger;
import com.bubbaTech.api.like.LikeNotFoundException;
import com.bubbaTech.api.like.LikeService;
import com.bubbaTech.api.store.Store;
import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.store.StoreService;
import com.bubbaTech.api.user.Gender;
import com.bubbaTech.api.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
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
@Transactional
@RequiredArgsConstructor
public class ClothingService {
    private final static int MAX_RANDS = 100;
    private final static int WEEKS_AGO = 3;

    @NonNull
    private final ClothingRepository repository;
    @NonNull
    private final LikeService likeService;
    @NonNull
    private final UserService userService;
    @NonNull
    private final StoreService storeService;
    @NonNull
    private final ModelMapper modelMapper;
    @NonNull
    private final ServiceLogger logger;

    @Value("${system.recommendation_system_addr}")
    public String recommendationSystemAddr;


    public ClothingDTO getById(long clothingId) throws ClothingNotFoundException {
        Optional<Clothing> item = repository.findById(clothingId);
        if (item.isEmpty())
            throw new ClothingNotFoundException(clothingId);
        return modelMapper.map(item.get(), ClothingDTO.class);
    }

    public List<ClothingDTO> recommendClothingIdList(long userId, String typeFilter, String genderFilter){
        Gender gender = genderStringToEnum(userId, genderFilter);
        List<ClothType> typeFilters = typeStringToList(typeFilter);
        try {
            StringBuilder urlString = new StringBuilder("http://" + recommendationSystemAddr +
                    "/recommendationList?userId=" + URLEncoder.encode(Long.toString(userId), StandardCharsets.UTF_8)+
                    "&gender=" + URLEncoder.encode(Integer.toString(gender.getIntValue()), StandardCharsets.UTF_8));
            if (typeFilters != null) {
                urlString.append("&clothingType=");
                for (ClothType clothType : typeFilters) {
                    urlString.append(URLEncoder.encode(Integer.toString(clothType.getIntValue()), StandardCharsets.UTF_8)).append(",");
                }
                urlString.deleteCharAt(urlString.length() - 1);
            }
            URL url = new URL(urlString.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMessage = "Unable to connect to recommendation system.";
                logger.error(errorMessage);
                throw new Exception(errorMessage);
            }

            System.out.println("Response Start");
            JSONObject jsonResponse = getConnectionResponse(connection);
            JSONArray clothingIdArray = (JSONArray) jsonResponse.get("clothingIds");
            List<Long> idList = new ArrayList<>();
            System.out.println("Response End");

            for (Object id : clothingIdArray) {
                idList.add((Long) id);
            }

            System.out.println("Query Start");
            List<Clothing> items = new ArrayList<>();
            for (Long id : idList) {
                repository.findById(id).ifPresent(items::add);
            }

            if (!items.isEmpty()) {
                //Converts List<Clothing> to List<ClothingDTO>
                List<ClothingDTO> itemsDTO = new ArrayList<>();
                for (Clothing item : items) {
                    System.out.println("Converter");
                    itemsDTO.add(modelMapper.map(item, ClothingDTO.class));
                }
                return itemsDTO;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ClothingDTO getCard(long userId, String typeFilter, String genderFilter){
        return recommendClothingIdList(userId, typeFilter, genderFilter).get(0);
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
        try {
            likeService.findByClothingAndUser(item.getId(), userId);
            return true;
        } catch (LikeNotFoundException exception) {
            return false;
        }
    }

    public ClothingDTO update(ClothingDTO item) {
        ClothingDTO clothingDTOItem = this.findByUrl(item.getProductURL());
        Clothing clothingItem = modelMapper.map(clothingDTOItem, Clothing.class);
        clothingItem.setGender(item.getGender());
        clothingItem.setName(item.getName());
        clothingItem.setImageURL(item.getImageURL());
        return modelMapper.map(repository.save(clothingItem), ClothingDTO.class);
    }

    public ClothingDTO create(ClothingDTO item) {
        try {
            ClothingDTO clothingItem = this.findByUrl(item.getProductURL());
            return update(clothingItem);
        } catch (ClothingNotFoundException exception) {
            Clothing itemEntity = modelMapper.map(item, Clothing.class);
            return modelMapper.map(repository.save(itemEntity), ClothingDTO.class);
        }
    }

    public ClothingDTO findByUrl(String url) throws ClothingNotFoundException {
        Optional<Clothing> item = repository.findByProductUrl(url);
        if (item.isEmpty())
            throw new ClothingNotFoundException(url);
        return modelMapper.map(item.get(), ClothingDTO.class);
    }

    public List<storeStatDTO> getClothingPerStoreData() {
        List<StoreDTO> stores = storeService.getAll();
        List<storeStatDTO> storeStats = new ArrayList<>();
        for (StoreDTO storeDTO: stores) {
            Store store = modelMapper.map(storeDTO, Store.class);
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
                typeFilters.add(ClothType.stringToClothType(str));
        }
        return typeFilters;
    }

    private Gender genderStringToEnum(long userId, String genderFilter) {
        Gender gender;
        if (genderFilter == null)
            gender = userService.getGenderById(userId);
        else
            gender = Gender.stringToGender(genderFilter);
        return gender;
    }

    public static JSONObject getConnectionResponse(HttpURLConnection connection) throws IOException, ParseException {
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = inputStream.readLine()) != null) {
            responseBuilder.append(line);
        }
        inputStream.close();

        return (JSONObject) new JSONParser().parse(responseBuilder.toString());
    }
}


