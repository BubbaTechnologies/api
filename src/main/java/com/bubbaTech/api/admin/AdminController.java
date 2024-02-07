//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.admin;


import com.bubbaTech.api.actuator.LikeDataEndpoint;
import com.bubbaTech.api.actuator.RouteResponseTimeEndpoint;
import com.bubbaTech.api.aws.LambdaService;
import com.bubbaTech.api.clothing.ClothingService;
import com.bubbaTech.api.data.storeStatDTO;
import com.bubbaTech.api.info.ServiceLogger;
import com.bubbaTech.api.security.authorities.Authorities;
import com.bubbaTech.api.store.StoreDTO;
import com.bubbaTech.api.store.StoreService;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    @NonNull
    private UserService userService;
    @NonNull
    private ClothingService clothingService;
    @NonNull
    private StoreService storeService;
    @NonNull
    private ServiceLogger logger;
    @NonNull
    private RouteResponseTimeEndpoint routeResponseTimeEndpoint;
    @NonNull
    private LikeDataEndpoint likeDataEndpoint;
    @NonNull
    private LambdaService lambdaService;


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

        logger.info("Changed permissions of " + user.getUsername() + ", id: " + user.getId()  + ", to " + auth.toString());
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

    @Scheduled(cron = "0 59 21 * * *")
    public void sendMetricsEmails() {
        try {
            InputStream resource = new ClassPathResource("static/statEmailList.json").getInputStream();

            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(new InputStreamReader(resource));

            //Gets and sends executive emails
            JSONArray executiveEmails = (JSONArray) obj.get("executive");
            List<String> emails = new ArrayList<>();
            for (Object executiveEmail : executiveEmails) {
                emails.add((String) executiveEmail);
            }
            sendExecutiveEmail(emails);

            //Gets and sends business emails
            JSONArray businessEmails = (JSONArray) obj.get("business");
            emails = new ArrayList<>();
            for (Object businessEmail : businessEmails) {
                emails.add((String) businessEmail);
            }
            sendBusinessEmail(emails);

            //Gets and sends developer emails
            JSONArray developerEmails = (JSONArray) obj.get("developer");
            emails = new ArrayList<>();
            for (Object developerEmail : developerEmails) {
                emails.add((String) developerEmail);
            }
            sendDeveloperEmail(emails);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void sendExecutiveEmail(List<String> emails) {
        JSONObject object = new JSONObject();

        //New Sign Ups
        List<UserDTO> newSignUps = userService.lastDaySignUps();
        List<String> newSignUpEmails = new ArrayList<>();
        for (UserDTO user : newSignUps) {
            newSignUpEmails.add(user.getEmail());
        }

        object.put("New Sign Ups Count", newSignUpEmails.size());
        object.put("New Sign Ups", newSignUpEmails);

        //Weekly Active User
        List<UserDTO> weeklyUser = userService.lastWeekUsers();
        List<String> weeklyUserEmails = new ArrayList<>();
        for (UserDTO user : weeklyUser) {
            weeklyUserEmails.add(user.getEmail());
        }
        object.put("Weekly Active Users Count", weeklyUserEmails.size());
        object.put("Past Week Active Users",  weeklyUserEmails);
        object.put("Recommend to Like Metrics", likeDataEndpoint.getLikeMetrics());
        object.put("Page Click Metrics",  likeDataEndpoint.getPageClickMetrics());
        object.put("Average Response Times", routeResponseTimeEndpoint.getAverageResponseTimes());
        clothingService.getClothingPerStoreData();
        JSONArray storeArray = new JSONArray();
        List<storeStatDTO> storeStatDTOS = clothingService.getClothingPerStoreData();
        for (storeStatDTO storeStatDTO : storeStatDTOS) {
            storeArray.add(storeStatDTO.toMap());
        }
        object.put("Store Statistics", storeArray);
        for (String email : emails) {
            sendEmail(email, object);
        }
    }

    private void sendDeveloperEmail(List<String> emails) {
        JSONObject object = new JSONObject();
        clothingService.getClothingPerStoreData();
        JSONArray storeArray = new JSONArray();
        List<storeStatDTO> storeStatDTOS = clothingService.getClothingPerStoreData();
        for (storeStatDTO storeStatDTO : storeStatDTOS) {
            storeArray.add(storeStatDTO);
        }
        object.put("Recommend to Like Statistics", likeDataEndpoint.getLikeMetrics());
        object.put("Store Statistics", storeArray);
        for (String email : emails) {
            sendEmail(email, object);
        }
    }

    private void sendBusinessEmail(List<String> emails) {
        JSONObject object = new JSONObject();

        //New Sign Ups
        List<UserDTO> newSignUps = userService.lastDaySignUps();
        List<String> newSignUpEmails = new ArrayList<>();
        for (UserDTO user : newSignUps) {
            newSignUpEmails.add(user.getEmail());
        }

        object.put("New Sign Ups Count", newSignUpEmails.size());
        object.put("New Sign Ups", newSignUpEmails);

        //Weekly Active User
        List<UserDTO> weeklyUser = userService.lastWeekUsers();
        List<String> weeklyUserEmails = new ArrayList<>();
        for (UserDTO user : weeklyUser) {
            weeklyUserEmails.add(user.getEmail());
        }
        object.put("Weekly Active Users Count", weeklyUserEmails.size());
        object.put("Past Week Active Users",  weeklyUserEmails);
        for (String email : emails) {
            sendEmail(email, object);
        }
    }

    private void sendEmail(String email, JSONObject data) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("recipient", email);
        requestBody.put("subject", "App Metrics - " + LocalDate.now());
        requestBody.put("data", data);

        Boolean sent = lambdaService.useLambda("genericEmailFunction", requestBody);
        if (sent) {
            logger.info("Sent metric email to " + email + ".");
        }
    }
}
