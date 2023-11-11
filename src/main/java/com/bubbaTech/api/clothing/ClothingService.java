//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.data.storeStatDTO;
import com.bubbaTech.api.filterOptions.FilterOptionsDTO;
import com.bubbaTech.api.info.ServiceLogger;
import com.bubbaTech.api.mapping.Mapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.*;


@Service
@Transactional
@RequiredArgsConstructor
public class ClothingService {
    private final static int WEEKS_AGO = 3;

    @NonNull
    private final ClothingRepository repository;
    @NonNull
    private final UserService userService;
    @NonNull
    private final StoreService storeService;
    @NonNull
    private final ServiceLogger logger;
    @NonNull
    private final Mapper mapper;
    @Value("${system.recommendation_system_addr}")
    public String recommendationSystemAddr;


    public ClothingDTO getById(long clothingId) throws ClothingNotFoundException {
        Optional<Clothing> item = repository.findById(clothingId);
        if (item.isEmpty())
            throw new ClothingNotFoundException(clothingId);
        return mapper.clothingToClothingDTO(item.get());
    }

    //Returns list of ClothingDTO items based on clothingIds

    public List<ClothingDTO> getByIds(List<Long> clothingIds) throws ClothingNotFoundException {
        List<Optional<Clothing>> optionalItems = repository.getListByIds(clothingIds);
        List<ClothingDTO> clothingDTOList = new ArrayList<>();
        for (int i = 0; i < optionalItems.size(); i++) {
            Optional<Clothing> optionalItem = optionalItems.get(i);
            //Check if image exists
            if (optionalItem.isEmpty())
                throw new ClothingNotFoundException(clothingIds.get(i));
            //Convert to DTO
            ClothingDTO clothingDTOItem = mapper.clothingToClothingDTO(optionalItem.get());
            //Attempts to show the first image presented
            List<String> imageUrls = clothingDTOItem.getImageURL();
            if (imageUrls.size() > 1)
                clothingDTOItem.setImageURL(imageUrls.subList(imageUrls.size() - 2, imageUrls.size() - 1));
            clothingDTOList.add(clothingDTOItem);
        }

        return clothingDTOList;
    }


    public List<ClothingDTO> recommendClothingList(long userId, String typeFilter, String genderFilter, Boolean singleItem){
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

            JSONObject jsonResponse = getConnectionResponse(connection);
            JSONArray clothingIdArray = (JSONArray) jsonResponse.get("clothingIds");
            List<Long> idList = new ArrayList<>();

            if (singleItem) {
                idList.add((Long) clothingIdArray.get(0));
            } else {
                for (Object id : clothingIdArray) {
                    idList.add((Long) id);
                }
            }

            List<Optional<Clothing>> items = repository.getListByIds(idList);

            //Converts List<Clothing> to List<ClothingDTO>
            List<ClothingDTO> itemsDTO = new ArrayList<>();
            for (Optional<Clothing> item : items) {
                item.ifPresent(clothing -> itemsDTO.add(mapper.clothingToClothingDTO(clothing)));
            }

            return itemsDTO;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public ClothingDTO getCard(long userId, String typeFilter, String genderFilter){
        long startTime = System.currentTimeMillis();

        Gender gender = genderStringToEnum(userId, genderFilter);
        List<ClothType> typeFilters = typeStringToList(typeFilter);
        logger.info("ClothingService.getCard: After gender and type filter conversion: " + (System.currentTimeMillis() - startTime));
        try {
            StringBuilder urlString = new StringBuilder("http://" + recommendationSystemAddr +
                    "/recommendation?userId=" + URLEncoder.encode(Long.toString(userId), StandardCharsets.UTF_8)+
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

            JSONObject jsonResponse = getConnectionResponse(connection);
            logger.info("ClothingService.getCard: After request: " + (System.currentTimeMillis() - startTime));
            Long clothingId = (Long) jsonResponse.get("clothingId");

            //Gets clothing
            Optional<Clothing> item  = repository.findById(clothingId);
            //If present returns item or else returns null
            if (item.isPresent()) {
                ClothingDTO clothing = mapper.clothingToClothingDTO(item.get());
                logger.info("ClothingService.getCard: Returning: " + (System.currentTimeMillis() - startTime));
                return clothing;
            } else {
                String errorMessage = "Unable to find item with id:" + clothingId;
                logger.error(errorMessage);
                throw new Exception(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public ClothingDTO update(ClothingDTO item) {
        ClothingDTO clothingDTOItem = this.findByUrl(item.getProductURL());
        Clothing clothingItem = mapper.clothingDTOToClothing(clothingDTOItem);
        clothingItem.setGender(item.getGender());
        clothingItem.setName(item.getName());
        clothingItem.setImageURL(item.getImageURL());
        clothingItem.setDate(LocalDate.now());
        return mapper.clothingToClothingDTO(repository.save(clothingItem));
    }


    public ClothingDTO create(ClothingDTO item) {
        Optional<ClothingDTO> itemOptional = this.getIfExists(item);
        if (itemOptional.isPresent()) {
            return update(itemOptional.get());
        } else {
            Clothing itemEntity = mapper.clothingDTOToClothing(item);
            return mapper.clothingToClothingDTO(repository.save(itemEntity));
        }
    }


    public ClothingDTO findByUrl(String url) throws ClothingNotFoundException {
        Optional<Clothing> item = repository.findByProductUrl(url);
        if (item.isEmpty())
            throw new ClothingNotFoundException(url);
        return mapper.clothingToClothingDTO(item.get());
    }


    public Optional<ClothingDTO> getIfExists(ClothingDTO item) {
        Optional<Clothing> itemOptional = repository.getIfExists(item.getProductURL());
        return itemOptional.map(mapper::clothingToClothingDTO);

    }


    public List<storeStatDTO> getClothingPerStoreData() {
        List<StoreDTO> stores = storeService.getAll();
        List<storeStatDTO> storeStats = new ArrayList<>();
        for (StoreDTO storeDTO: stores) {
            Store store = mapper.storeDTOToStore(storeDTO);
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

    @Cacheable("filterOptions")
    public FilterOptionsDTO getFilterOptions() {
        //Calculate genders and types per gender
        List<Gender> genders = new ArrayList<>();
        List<List<ClothType>> typesPerGender = new ArrayList<>();

        LocalDate TIME_RESTRICTION = LocalDate.now().minusWeeks(WEEKS_AGO);

        for (Gender gender : Gender.values()) {
            if (repository.countByGender(gender, TIME_RESTRICTION) > 0) {
                genders.add(gender);
                List<ClothType> typeList = new ArrayList<>();
                for (ClothType type : ClothType.values()) {
                    if (repository.countByGenderAndTypes(gender, new ArrayList<>(List.of(type)), TIME_RESTRICTION) > 0) {
                        typeList.add(type);
                    }
                }
                typesPerGender.add(typeList);
            }
        }

        //Calculate tags per type
        Map<ClothType, List<ClothingTag>> tagsPerType = new HashMap<>();
        for (ClothType type :  ClothType.values()) {
            ArrayList<ClothingTag> tagList = new ArrayList<>();
            for (ClothingTag tag : ClothingTag.values()) {
                if (repository.countByTypeAndTag(type, tag, TIME_RESTRICTION) > 0) {
                    tagList.add(tag);
                }
            }
            tagsPerType.put(type, tagList);
        }

        return new FilterOptionsDTO(genders, typesPerGender, tagsPerType);
    }

    @CacheEvict(value = "filterOptions", allEntries = true)
    @Scheduled(fixedRateString = "${caching.spring.filterOptionsTTL}")
    public void filterOptionsEvict() {
        logger.info("Emptying filterOptions cache.");
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

    /**
     * @param storeName: String containing the name of the store.
     * @return: The amount of clothing the store collected within the last week.
     */
    public Optional<Long> getLastWeekCollection(String storeName) {
        //Gets store
        Optional<StoreDTO> storeDTO = storeService.getByName(storeName);
        if (storeDTO.isEmpty()) {
            return Optional.empty();
        }

        //Gets data on store
        Store store = mapper.storeDTOToStore(storeDTO.get());
        return Optional.of(repository.countByStoreAndDate(store, LocalDate.now().minusWeeks(1)));
    }
}


