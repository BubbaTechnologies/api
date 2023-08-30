//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.app.AppController;
import com.bubbaTech.api.data.storeStatDTO;
import com.bubbaTech.api.filterOptions.FilterOptionsDTO;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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

    @Value("${system.image_processing_addr}")
    private String imageProcessingAddr;

    public ClothingDTO getById(long clothingId) throws ClothingNotFoundException {
        Optional<Clothing> item = repository.findById(clothingId);
        if (item.isEmpty())
            throw new ClothingNotFoundException(clothingId);
        return modelMapper.map(item.get(), ClothingDTO.class);
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

            List<Clothing> items = new ArrayList<>();
            StringBuilder idQuery = new StringBuilder();
            for (Long id : idList) {
                repository.findById(id).ifPresent(items::add);
                idQuery.append(id).append(",");
            }
            idQuery = new StringBuilder(idQuery.substring(0, idQuery.length() - 2));

            if (!items.isEmpty()) {
                //Converts List<Clothing> to List<ClothingDTO>
                List<ClothingDTO> itemsDTO = new ArrayList<>();
                try {
                    url = new URL("http://" + imageProcessingAddr + "/images?clothingIds=" + idQuery);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    for (Clothing item : items) {
                        ClothingDTO clothingDTO = modelMapper.map(item, ClothingDTO.class);
                        if (connection.getResponseCode() == 200) {
                            convertDTOToImageUrls(clothingDTO);
                        }
                        itemsDTO.add(clothingDTO);
                    }
                } catch (Exception e) {
                    logger.error("Could not connect to image processing server at " + imageProcessingAddr);
                    for (Clothing item : items) {
                        itemsDTO.add( modelMapper.map(item, ClothingDTO.class));
                    }
                }

                return itemsDTO;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ClothingDTO getCard(long userId, String typeFilter, String genderFilter){
        return recommendClothingList(userId, typeFilter, genderFilter, true).get(0);
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

    private void convertDTOToImageUrls(ClothingDTO clothing) {
        List<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < clothing.getImageURL().size(); i++) {
            imageUrls.add(linkTo(AppController.class)
                    .slash("/image")
                    .toUriComponentsBuilder().scheme("https")
                    .queryParam("clothingId", clothing.getId())
                    .queryParam("imageId", i)
                    .toUriString());
        }
        clothing.setImageURL(imageUrls);
    }
}


