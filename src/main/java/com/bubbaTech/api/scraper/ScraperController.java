//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.scraper;


import com.bubbaTech.api.clothing.ClothingDTO;
import com.bubbaTech.api.clothing.ClothingNotFoundException;
import com.bubbaTech.api.clothing.ClothingService;
import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.store.StoreService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/scraper")
public class ScraperController {
    private ClothingService clothingService;
    private StoreService storeService;


    @GetMapping(value = "/checkStore", produces = "application/json", params = {"url"})
    public ResponseEntity<?> checkStore(@RequestParam(name = "url") String storeUrl) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Optional<StoreDTO> store = storeService.getByUrl(storeUrl);

        if (store.isEmpty())
            return ResponseEntity.ok().headers(headers).body("{}");
        return ResponseEntity.ok().headers(headers).body(store.get());
    }

    @GetMapping(value = "/checkClothing", produces = "application/json", params = {"url"})
    public ResponseEntity<?> checkLink(@RequestParam(name = "url") String productUrl) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ClothingDTO item = clothingService.findByUrl(productUrl);
            return ResponseEntity.ok().headers(headers).body(item);
        } catch (ClothingNotFoundException exception) {
            return ResponseEntity.ok().headers(headers).body("{}");
        }
    }

    @PostMapping(value = "/store", produces = "application/json")
    public ResponseEntity<?> createStore(@RequestBody StoreDTO store) {
        store = storeService.create(store);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(store);
    }

    @PostMapping(value = "/clothing", produces = "application/json")
    public ResponseEntity<?> createClothing(@RequestBody ClothingDTO clothing) {
        clothing = clothingService.create(clothing);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(clothing);
    }
}
