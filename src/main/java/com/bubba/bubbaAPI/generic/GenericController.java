//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.generic;

import com.bubba.bubbaAPI.security.authentication.CustomUserDetailsService;
import com.bubba.bubbaAPI.security.authentication.JwtUtil;
import com.bubba.bubbaAPI.security.authentication.model.AuthenticationRequest;
import com.bubba.bubbaAPI.security.authentication.model.AuthenticationResponse;
import com.bubba.bubbaAPI.user.User;
import com.bubba.bubbaAPI.user.UserDTO;
import com.bubba.bubbaAPI.user.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }
        final User userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return ResponseEntity.ok(new AuthenticationResponse(jwt.generateToken(userDetails), userDetails.getName(), userDetails.getUsername()));
    }


    @GetMapping("/")
    public ResponseEntity<?> home(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", "https://www.bubba-app.com");
        httpServletResponse.setStatus(302);
        return ResponseEntity.status(302).build();
    }

    @PostMapping(value = "/create", produces = "application/json")
    public ResponseEntity<?> create(@RequestBody UserDTO newUser) throws Exception {
        UserDTO user = modelMapper.map(userService.create(modelMapper.map(newUser, User.class)), UserDTO.class);

        AuthenticationRequest request = new AuthenticationRequest(user.getUsername(), newUser.getPassword());
        return this.login(request);
    }

    @PutMapping(value = "/update", produces = "application/json")
    public EntityModel<UserDTO> update(@RequestParam UserDTO userRequest, Principal principal) {
        User loggedUser = userDetailsService.loadUserByUsername(principal.getName());
        User update = modelMapper.map(userRequest, User.class);
        update.setId(loggedUser.getId());
        update.setEnabled(loggedUser.getEnabled());
        update.setAccountExpiration(loggedUser.getAccountExpiration());
        update.setCredentialExpiration(loggedUser.getCredentialExpiration());
        update.setGrantedAuthorities(loggedUser.getGrantedAuthorities());
        userRequest = modelMapper.map(userService.update(update), UserDTO.class);
        return EntityModel.of(userRequest,
                linkTo("/").withRel("home"));
    }


    @DeleteMapping(value = "/delete")
    ResponseEntity<?> delete(Principal principal) {
        User user = userDetailsService.loadUserByUsername(principal.getName());
        user.setEnabled(false);
        userService.update(user);
        return ResponseEntity.ok().build();
    }
}
