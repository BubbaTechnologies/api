//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.security.authentication;

import com.bubbaTech.api.user.User;
import com.bubbaTech.api.user.UserDTO;
import com.bubbaTech.api.user.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    private final ModelMapper modelMapper;

    CustomUserDetailsService(@Lazy UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return modelMapper.map(userService.getByUsername(username),User.class);
    }

    public UserDTO loadUserByUsernameToDTO(String username) throws UsernameNotFoundException {
        return userService.getByUsername(username);
    }
}
