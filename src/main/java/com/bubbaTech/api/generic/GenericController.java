//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.generic;

import com.bubbaTech.api.aws.LambdaService;
import com.bubbaTech.api.info.ServiceLogger;
import com.bubbaTech.api.security.authentication.CustomUserDetailsService;
import com.bubbaTech.api.security.authentication.JwtUtil;
import com.bubbaTech.api.security.authentication.model.AuthenticationRequest;
import com.bubbaTech.api.security.authentication.model.AuthenticationResponse;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GenericController {
    @NonNull
    private AuthenticationManager auth;
    @NonNull
    private JwtUtil jwt;
    @NonNull
    private CustomUserDetailsService userDetailsService;
    @NonNull
    private UserService userService;
    @NonNull
    private final ServiceLogger logger;
    @NonNull
    private LambdaService lambdaService;

    @GetMapping(value = "/error")
    public ResponseEntity<?> error() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/health")
    public ResponseEntity<?> check() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<?> home(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", "https://www.peachsconemarket.com");
        httpServletResponse.setStatus(302);
        return ResponseEntity.status(302).build();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request)  {
        try {
            auth.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        final UserDTO userDetails = userDetailsService.loadUserByUsernameToDTO(request.getUsername());
        userService.updateLastLogin(userDetails.getId());
        return ResponseEntity.ok(new AuthenticationResponse(jwt.generateToken(userDetails), userDetails.getUsername()));
    }

    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<?> create(@RequestBody UserDTO newUser, @RequestParam String verificationCode) {
        if (!generateVerificationCode(newUser.getUsername()).equals(verificationCode)) {
            return ResponseEntity.unprocessableEntity().build();
        }


        UserDTO user;
        try {
            user = userService.create(newUser);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        AuthenticationRequest request = new AuthenticationRequest(user.getUsername(), newUser.getPassword());
        return this.login(request);
    }

    @PostMapping(value = "/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> map) {
        if (!map.containsKey("email")) {
            return ResponseEntity.unprocessableEntity().build();
        }
        String userEmail = map.get("email");

        if (userEmail.length() < 4) {
            return ResponseEntity.unprocessableEntity().build();
        }

        String verificationCode = generateVerificationCode(userEmail);

        JSONObject requestBody = new JSONObject();
        requestBody.put("recipient", userEmail);
        requestBody.put("verificationCode", verificationCode.toString());

        Boolean sent = lambdaService.useLambda("verificationEmailFunction", requestBody);
        if (sent) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.internalServerError().build();
    }

    @PutMapping(value = "/update", produces = "application/json")
    public EntityModel<UserDTO> update(@RequestBody UserDTO userRequest, Principal principal) {
        UserDTO loggedUser = userDetailsService.loadUserByUsernameToDTO(principal.getName());
        UserDTO update = userRequest;
        update.setId(loggedUser.getId());
        update.setEnabled(loggedUser.getEnabled());
        update.setGrantedAuthorities(loggedUser.getGrantedAuthorities());
        userRequest = userService.update(update);
        return EntityModel.of(userRequest);
    }


    @DeleteMapping(value = "/delete")
    ResponseEntity<?> delete(Principal principal) {
        UserDTO user = userDetailsService.loadUserByUsernameToDTO(principal.getName());
        user.setEnabled(false);
        userService.update(user);
        return ResponseEntity.ok().build();
    }

    private String generateVerificationCode(String email) {
        //Generate verification code
        StringBuilder verificationCode = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            verificationCode.append((int) email.charAt(i) - 32);
        }

        return verificationCode.toString();
    }
}
