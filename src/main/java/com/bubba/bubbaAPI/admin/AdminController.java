//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.admin;


import com.bubba.bubbaAPI.security.authorities.Authorities;
import com.bubba.bubbaAPI.store.Store;
import com.bubba.bubbaAPI.store.StoreDTO;
import com.bubba.bubbaAPI.store.StoreService;
import com.bubba.bubbaAPI.user.User;
import com.bubba.bubbaAPI.user.UserDTO;
import com.bubba.bubbaAPI.user.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@AllArgsConstructor
public class AdminController {
    UserService userService;
    ModelMapper modelMapper;

    StoreService storeService;

    @RequestMapping(value = "/admin/permissions", method = RequestMethod.POST)
    public ResponseEntity<?> changePermission(@RequestBody Map<String, String> permissions) {
        String[] newPermissions = permissions.get("permissions").split(", ");
        User user = userService.getByUsername(permissions.get("username"));
        Collection<Authorities> auth = user.getGrantedAuthorities();

        for (String perm : newPermissions)
            auth.add(new Authorities(perm));

        userService.update(user);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(modelMapper.map(user, UserDTO.class));
    }


    @PutMapping(value = "/admin/disableStore", params = {"store"})
    public ResponseEntity<?> changePermission(@RequestParam(value = "store") String storeName) {
        Store store = storeService.disableStore(storeName);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(modelMapper.map(store, StoreDTO.class));
    }
}
