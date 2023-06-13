//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.generic;

import com.bubbaTech.api.security.authentication.CustomUserDetailsService;
import com.bubbaTech.api.security.authentication.JwtUtil;
import com.bubbaTech.api.security.authentication.model.AuthenticationRequest;
import com.bubbaTech.api.security.authentication.model.AuthenticationResponse;
import com.bubbaTech.api.user.User;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@AllArgsConstructor
public class GenericController {
    private AuthenticationManager auth;
    private JwtUtil jwt;
    private CustomUserDetailsService userDetailsService;
    private ModelMapper modelMapper;
    private UserService userService;

    @GetMapping(value = "/error")
    public ResponseEntity<?> error() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/health")
    public ResponseEntity<?> check() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) throws Exception {
        try {
            auth.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        final User userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return ResponseEntity.ok(new AuthenticationResponse(jwt.generateToken(userDetails), userDetails.getName(), userDetails.getUsername()));
    }


    @GetMapping("/")
    public ResponseEntity<?> home(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", "https://www.peachsconemarket.com");
        httpServletResponse.setStatus(302);
        return ResponseEntity.status(302).build();
    }

    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<?> create(@RequestBody UserDTO newUser) throws Exception {
        UserDTO user;
        try {
            user = modelMapper.map(userService.create(modelMapper.map(newUser, User.class)), UserDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        AuthenticationRequest request = new AuthenticationRequest(user.getUsername(), newUser.getPassword());
        return this.login(request);
    }

    @PutMapping(value = "/update", produces = "application/json")
    public EntityModel<UserDTO> update(@RequestBody UserDTO userRequest, Principal principal) {
        User loggedUser = userDetailsService.loadUserByUsername(principal.getName());
        User update = modelMapper.map(userRequest, User.class);
        update.setId(loggedUser.getId());
        update.setEnabled(loggedUser.getEnabled());
        update.setAccountExpiration(loggedUser.getAccountExpiration());
        update.setCredentialExpiration(loggedUser.getCredentialExpiration());
        update.setGrantedAuthorities(loggedUser.getGrantedAuthorities());
        userRequest = modelMapper.map(userService.update(update), UserDTO.class);
        return EntityModel.of(userRequest);
    }


    @DeleteMapping(value = "/delete")
    ResponseEntity<?> delete(Principal principal) {
        User user = userDetailsService.loadUserByUsername(principal.getName());
        user.setEnabled(false);
        userService.update(user);
        return ResponseEntity.ok().build();
    }
}
