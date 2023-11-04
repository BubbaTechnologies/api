//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.mapping.Mapper;
import com.bubbaTech.api.security.authorities.Authorities;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private final Mapper mapper;
    public UserService(@Lazy UserRepository repository, @Lazy PasswordEncoder passwordEncoder, Mapper mapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }


    public UserDTO getById(long userId) throws UserNotFoundException {
        Optional<User> user = repository.findById(userId);
        if (user.isPresent())
            return mapper.userToUserDTO(user.get());
        else
            throw new UserNotFoundException(String.format("Could not find user with id %d", userId));
    }

    public Gender getGenderById(long userId) {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getGender();
    }

    public void authUser(String username, String password) throws AuthenticationServiceException {
        UserDTO user = getByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new AuthenticationServiceException("Could not authenticate");
    }


    public UserDTO getByUsername(String username) {
        return mapper.userToUserDTO(repository.findByEmail(username).orElseThrow(() -> new UserNotFoundException(username)));
    }

    public Boolean checkUsername(String username) {
        return repository.findByEmail(username).isPresent();
    }


    public UserDTO create(UserDTO userRequest) {
        if (this.checkUsername(userRequest.getUsername()))
            throw new UserExistsException(userRequest.getUsername());
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setGender(userRequest.getGender());
        user.setEnabled(userRequest.getEnabled());
        user.setAccountCreated(LocalDate.now());
        user.setLastLogin(LocalDate.now());
        Collection<Authorities> auth = new ArrayList<>();
        auth.add(new Authorities("USER"));
        user.setGrantedAuthorities(auth);
        user = repository.save(user);

        return mapper.userToUserDTO(user);
    }


    public UserDTO update(UserDTO userRequest) {
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
        user.setGrantedAuthorities(userRequest.getGrantedAuthorities());

        return mapper.userToUserDTO(repository.save(user));
    }


    public void updateLastLogin(long userId) {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        user.setLastLogin(LocalDate.now());
        repository.save(user);
    }


    public void delete(User user) {
        repository.delete(user);
    }

}
