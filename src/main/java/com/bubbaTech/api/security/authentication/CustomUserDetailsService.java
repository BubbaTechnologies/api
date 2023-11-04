//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.security.authentication;

import com.bubbaTech.api.mapping.Mapper;
import com.bubbaTech.api.user.User;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    private final Mapper mapper;

    CustomUserDetailsService(@Lazy UserService userService, Mapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return mapper.userDTOToUser(userService.getByUsername(username));
    }

    public UserDTO loadUserByUsernameToDTO(String username) throws UsernameNotFoundException {
        return userService.getByUsername(username);
    }
}
