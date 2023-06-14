//package com.bubbaTech.api.security.authentication;
//
//import lombok.AllArgsConstructor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.stereotype.Component;
//
//@Component
//@AllArgsConstructor
//public class CustomAuthenticationManager implements AuthenticationManager {
//
//    private UpAuthenticationProvider authenticationProvider;
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        return authenticationProvider.authenticate(authentication);
//    }
//}
