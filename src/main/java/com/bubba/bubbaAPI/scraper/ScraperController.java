//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.scraper;


import com.bubba.bubbaAPI.clothing.Clothing;
import com.bubba.bubbaAPI.clothing.ClothingDTO;
import com.bubba.bubbaAPI.clothing.ClothingService;
import com.bubba.bubbaAPI.store.Store;
import com.bubba.bubbaAPI.store.StoreDTO;
import com.bubba.bubbaAPI.store.StoreService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
public class ScraperController {

    private ClothingService clothingService;

    private StoreService storeService;

    private ModelMapper modelMapper;


    @GetMapping(value = "/scraper/checkStore", produces = "application/json", params = {"url"})
    public ResponseEntity<?> checkStore(@RequestParam(name = "url") String storeUrl) {
        Optional<Store> store = storeService.findByUrl(storeUrl);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!store.isPresent())
            return ResponseEntity.ok().headers(headers).body("{}");
        return ResponseEntity.ok().headers(headers).body(modelMapper.map(store, StoreDTO.class));
    }

    @GetMapping(value = "/scraper/checkClothing", produces = "application/json", params = {"url"})
    public ResponseEntity<?> checkLink(@RequestParam(name = "url") String productUrl) {
        Optional<Clothing> item = clothingService.findByUrl(productUrl);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!item.isPresent())
            return ResponseEntity.ok().headers(headers).body("{}");
        return ResponseEntity.ok().headers(headers).body(modelMapper.map(item, ClothingDTO.class));
    }

    @PostMapping(value = "/scraper/store", produces = "application/json")
    public ResponseEntity<?> createStore(@RequestBody StoreDTO store) {
        store = modelMapper.map(storeService.create(modelMapper.map(store, Store.class)), StoreDTO.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(store);
    }

    @PostMapping(value = "/scraper/clothing", produces = "application/json")
    public ResponseEntity<?> createClothing(@RequestBody ClothingDTO clothing) {
        clothing = modelMapper.map(clothingService.create(modelMapper.map(clothing, Clothing.class)), ClothingDTO.class);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(clothing);
    }
}
