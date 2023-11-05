//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.admin;


import com.bubbaTech.api.actuator.RouteResponseTimeEndpoint;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.File;
import java.io.FileReader;
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

    @Value("${system.aws.accessKey}")
    private String accessKey;
    @Value("${system.aws.privateKey}")
    private String privateKey;

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

    @Scheduled(fixedRateString = "${caching.spring.filterOptionsTTL}")
    public void sendMetricsEmails() {
        try {
            File resource = new ClassPathResource("static/statEmailList.json").getFile();

            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(new FileReader(resource));

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
            for (Object executiveEmail : executiveEmails) {
                emails.add((String) executiveEmail);
            }
            sendBusinessEmail(emails);

            //Gets and sends developer emails
            JSONArray developerEmails = (JSONArray) obj.get("developer");
            emails = new ArrayList<>();
            for (Object executiveEmail : executiveEmails) {
                emails.add((String) executiveEmail);
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
            newSignUpEmails.add(user.getUsername());
        }

        object.put("New Sign Ups Count", newSignUpEmails.size());
        object.put("New Sign Ups", newSignUpEmails);

        //Weekly Active User
        List<UserDTO> weeklyUser = userService.lastWeekUsers();
        List<String> weeklyUserEmails = new ArrayList<>();
        for (UserDTO user : weeklyUser) {
            weeklyUserEmails.add(user.getUsername());
        }
        object.put("Weekly Active Users Count", weeklyUserEmails.size());
        object.put("Past Week Active Users",  weeklyUserEmails);

        clothingService.getClothingPerStoreData();
        JSONArray storeArray = new JSONArray();
        List<storeStatDTO> storeStatDTOS = clothingService.getClothingPerStoreData();
        for (storeStatDTO storeStatDTO : storeStatDTOS) {
            storeArray.add(storeStatDTO.toString());
        }
        object.put("Store Stats", storeArray);
        object.put("Average Response Times", routeResponseTimeEndpoint.getAverageResponseTimes().toString());

        for (String email : emails) {
            sendEmail(email, object);
        }
    }

    private void sendDeveloperEmail(List<String> emails) {
        //TODO
    }

    private void sendBusinessEmail(List<String> emails) {
        //TODO
    }

    private void sendEmail(String email, JSONObject data) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("recipient", email);
        requestBody.put("subject", "App Metrics - " + LocalDate.now().toString());
        requestBody.put("data", data);

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, privateKey);
            LambdaClient lambdaClient = LambdaClient.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName("genericEmailFunction")
                    .payload(SdkBytes.fromUtf8String(requestBody.toJSONString()))
                    .build();

            InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
            if (invokeResponse.statusCode() != 200) {
                String errorMessage = "Unable to connect to emailClient";
                logger.error(errorMessage);
                throw new Exception(errorMessage);
            } else {
                logger.info("Sent metric email to " + email + ".");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
