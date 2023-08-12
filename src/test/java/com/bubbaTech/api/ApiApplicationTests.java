package com.bubbaTech.api;

import com.bubbaTech.api.clothing.ClothType;
import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.clothing.ClothingSerializer;
import com.bubbaTech.api.clothing.ClothingTag;
import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.user.Gender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ApiApplicationTests {
    //TODO: Write tests
    @Test
    void contextLoads() {
    }

    @Test
    public void serializeTest() throws JsonProcessingException {
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setId(1L);
        storeDTO.setName("test Store");
        storeDTO.setURL("test.com");
        List<String> urls = new ArrayList<>();
        List<ClothingTag> tags = new ArrayList<>();
        ClothingDTO clothingDTO = new ClothingDTO("test", urls, "test", storeDTO, ClothType.OTHER, Gender.MALE, tags);
        clothingDTO.setId(1L);
        clothingDTO.setDate(LocalDate.now());
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ClothingDTO.class, new ClothingSerializer());
        mapper.registerModule(module);
        System.out.println(EntityModel.of(clothingDTO));
    }
}
