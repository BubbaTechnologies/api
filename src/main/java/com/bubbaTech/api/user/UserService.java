//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.security.authorities.Authorities;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;


    public UserService(@Lazy UserRepository repository, @Lazy PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }


    public Optional<User> getById(long userId) {
        return repository.findById(userId);
    }

    public Gender getGenderById(long userId) {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getGender();
    }

    public User authUser(String username, String password) throws Exception {
        User user = getByUsername(username);
        if (passwordEncoder.matches(password, user.getPassword()))
            return user;
        else
            throw new AuthenticationServiceException("Could not authenticate");
    }

    public User getByUsername(String username) {
        return repository.findByEmail(username).orElseThrow(() -> new UserNotFoundException(username));
    }

    public Optional<User> checkUsername(String username) {
        return repository.findByEmail(username);
    }

    public User create(User user) {
        if (this.checkUsername(user.getUsername()).isPresent())
            throw new UserExistsException(user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Collection<Authorities> auth = new ArrayList<>();
        auth.add(new Authorities("USER"));
        user.setGrantedAuthorities(auth);
        user = repository.save(user);

        return user;
    }

    public User update(User userRequest) {
        User user = repository.findById(userRequest.getId()).orElseThrow(() -> new UserNotFoundException(userRequest.getId()));

        user.setId(userRequest.getId());
        if (userRequest.getPassword().equals(user.getPassword())) {
            user.setPassword(user.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        user.setUsername(userRequest.getUsername());
        user.setGender(userRequest.getGender());
        user.setEnabled(userRequest.getEnabled());
        user.setAccountExpiration(userRequest.getAccountExpiration());
        user.setCredentialExpiration(userRequest.getCredentialExpiration());
        user.setGrantedAuthorities(userRequest.getGrantedAuthorities());

        return repository.save(user);
    }

    public void delete(User user) {
        repository.delete(user);
    }

}
