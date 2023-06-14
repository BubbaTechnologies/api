//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.admin;


import com.bubbaTech.api.clothing.ClothingService;
import com.bubbaTech.api.security.authorities.Authorities;
import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.store.StoreService;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    UserService userService;
    ClothingService clothingService;
    StoreService storeService;

    @RequestMapping(value = "/permissions", method = RequestMethod.POST)
    public ResponseEntity<?> changePermission(@RequestBody Map<String, String> permissions) {
        String[] newPermissions = permissions.get("permissions").split(", ");
        UserDTO user = userService.getByUsername(permissions.get("username"));
        Collection<Authorities> auth = user.getGrantedAuthorities();

        for (String perm : newPermissions)
            auth.add(new Authorities(perm));

        userService.update(user);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(user);
    }

    @GetMapping(value = "/storeStats")
    public ResponseEntity<?> getClothingData() {
        return ResponseEntity.ok().body(clothingService.getClothingPerStoreData());
    }


    @PutMapping(value = "/disableStore", params = {"store"})
    public ResponseEntity<?> changePermission(@RequestParam(value = "store") String storeName) {
        StoreDTO store = storeService.disableStore(storeName);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(store);
    }
}
