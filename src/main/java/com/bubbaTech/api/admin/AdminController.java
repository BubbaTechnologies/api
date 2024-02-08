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
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.util.*;

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

    @Value("${apn.key_id}")
    public String key_id;

    @Value("${apn.team_id}")
    public String team_id;
    @Value("${apn.private_key}")
    public String private_key;

    private static final Logger notificationLogger = LoggerFactory.getLogger(AdminController.class);


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

    /**
     * Sends notification to all users.
     * @param principal: User sending request
     * @param notificationData: Data that will represent the notification.
     * @return: 200 if successful.
     */
    @PostMapping(value = "/sendNotification")
    public ResponseEntity<?> sendNotificationRoute(Principal principal, @RequestBody Map<String, ?> notificationData) {
        if (!notificationData.containsKey("priority") || !notificationData.containsKey("title") ||  !notificationData.containsKey("body")) {
            return ResponseEntity.unprocessableEntity().build();
        }
        UserDTO requestingUser = userService.getByUsername(principal.getName());
        Integer priority = (Integer) notificationData.get("priority");
        String title = (String) notificationData.get("title");
        String body = (String) notificationData.get("body");

        notificationLogger.info("Notification sent by " + requestingUser.getId() + "\nTitle: "
                + title + "\nBody: " + body);

        Map<String, String> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("body", body);

        List<String> deviceIds = userService.getAllDeviceIds();
        for (String deviceId : deviceIds) {
            if (!sendNotification(deviceId, priority, payload)) {
                notificationLogger.error("Failed to send a notification to device id " + deviceId + ".");
            }
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/disableStore", params = {"store"})
    public ResponseEntity<?> changePermission(@RequestParam(value = "store") String storeName) {
        StoreDTO store = storeService.disableStore(storeName);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().headers(headers).body(store);
    }

    @Scheduled(cron = "0 0 14 * * *")
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

    private static PrivateKey loadPrivateKey(String privateKeyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyString.getBytes());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(spec);
    }

    public String generateJWT(String privateKey) throws Exception {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(key_id).build();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(team_id)
                .issueTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        JWSSigner signer = new ECDSASigner((ECPrivateKey) AdminController.loadPrivateKey(private_key));
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    @Async
    public boolean sendNotification(String deivceId, Integer priority, Map<String, String> payload) {
        try {
            StringBuilder urlString = new StringBuilder("https://api.push.apple.com:443/3/device/" + deivceId);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(urlString.toString()))
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(payload)))
                    .header("Content-Type","application/json")
                    .header("apns-priority", priority.toString())
                    .header("apns-expiration", "0")
                    .header("apns-push-type", "alert")
                    .header("apns-topic", "Bubba-Technologies-Inc.Carou")
                    .header("authorization", "bearer " + generateJWT(private_key));

           HttpRequest request = requestBuilder.build();
           HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode != 200) {
                logger.error("Response Body: " + response.body());
                throw new Exception("Did not successfully send APN request.");
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private String buildRequestBody(Map<String, String> payload) {
        JSONObject jsonObjectAlert = new JSONObject();
        jsonObjectAlert.put("alert", payload);
        JSONObject jsonObjectAps = new JSONObject();
        jsonObjectAps.put("aps", jsonObjectAlert);

        return jsonObjectAps.toJSONString();
    }
}
