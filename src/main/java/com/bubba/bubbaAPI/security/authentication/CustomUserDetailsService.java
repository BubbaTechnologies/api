//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.security.authentication;

import com.bubba.bubbaAPI.user.User;
import com.bubba.bubbaAPI.user.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    CustomUserDetailsService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService.getByUsername(username);
    }
}
