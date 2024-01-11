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
import java.util.List;
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
            throw new AuthenticationServiceException("Could not authenticate.");
    }

    public List<UserDTO> getFollowing(long userId) {
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user: repository.getReferenceById(userId).getFollowing()) {
            userDTOS.add(mapper.userToUserDTO(user));
        }

        return userDTOS;
    }

    public List<UserDTO> getFollowers(long userId) {
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user: repository.getReferenceById(userId).getFollowers()) {
            userDTOS.add(mapper.userToUserDTO(user));
        }

        return userDTOS;
    }

    public UserDTO getByUsername(String username) {
        return mapper.userToUserDTO(repository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username)));
    }

    public Boolean checkUsername(String username) {
        return repository.countByUsername(username) > 0;
    }

    /**
     * Checks if the email exists within the repository.
     * @param email: The email being checked.
     * @return: True if exists and false otherwise.
     */
    public Boolean checkEmail(String email) {
        return repository.countByEmail(email) > 0;
    }

    /**
     * Follows requested user.
     * @param requesterUserId: Requester user ID.
     * @param requestedUserId: Requested user to follow User ID.
     */
    public void followUser(Long requesterUserId, Long requestedUserId) {
        User requesterUser = repository.getReferenceById(requesterUserId);
        User requestedUser = repository.getReferenceById(requestedUserId);

        if (requestedUser.getPrivateAccount() && !requestedUser.getFollowers().contains(requesterUser)) {
            List<User> followRequests = requestedUser.getFollowRequests();
            if (!followRequests.contains(requesterUser)) {
                followRequests.add(requesterUser);

                requestedUser.setFollowRequests(followRequests);
            }
        } else {
            addFollowing(requesterUser, requestedUser);
        }
    }

    /**
     * Unfollows/unrequests requested user.
     * @param requesterUserId: User id requesting to unfollow.
     * @param requestedUserId: User id that is being requested to unfollow.
     */
    public void unfollowUser(Long requesterUserId, Long requestedUserId) {
        User requesterUser = repository.getReferenceById(requesterUserId);
        User requestedUser = repository.getReferenceById(requestedUserId);

        List<User> requests = requestedUser.getFollowRequests();
        if (requests.contains(requesterUser)) {
            requests.remove(requesterUser);
        } else {
            List<User> followers = requestedUser.getFollowers();
            followers.remove(requesterUser);
            requestedUser.setFollowers(followers);

            List<User> following = requesterUser.getFollowing();
            following.remove(requestedUser);
            requesterUser.setFollowing(following);
        }

        repository.save(requesterUser);
        repository.save(requestedUser);
    }

    /**
     * Applies approved to follow request.
     * @param requestedUserId: User ID being requested to follow.
     * @param requesterUserId: User ID requesting to follow.
     * @param approved: True if approved.
     * @return: True if successful. False otherwise.
     */
    public Boolean requestAction(Long requestedUserId, Long requesterUserId, Boolean approved) {
        User requesterUser = repository.getReferenceById(requesterUserId);
        User requestedUser = repository.getReferenceById(requestedUserId);

        //Checks if requester requested
        List<User> requests = requestedUser.getFollowRequests();
        if (!requests.contains(requesterUser)) {
            return false;
        }

        requests.remove(requesterUser);

        if (approved) {
            addFollowing(requesterUser, requestedUser);
        }

        return true;
    }

    private void addFollowing(User requesterUser, User requestedUser) {
        List<User> followers = requestedUser.getFollowers();
        if (!followers.contains(requesterUser)) {
            followers.add(requesterUser);
            requestedUser.setFollowers(followers);

            List<User> following = requesterUser.getFollowing();
            following.add(requestedUser);
            requesterUser.setFollowing(following);
        }
    }

    public UserDTO create(UserDTO userRequest) {
        if (this.checkUsername(userRequest.getUsername()) || this.checkEmail(userRequest.getEmail()))
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
        user.setBirthDate(userRequest.getBirthDate());
        user.setLatitude(userRequest.getLatitude());
        user.setLongitude(userRequest.getLongitude());
        user.setEmail(userRequest.getEmail());
        user.setPrivateAccount(false);
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
        user.setLongitude(userRequest.getLongitude());
        user.setLatitude(userRequest.getLatitude());
        user.setBirthDate(userRequest.getBirthDate());
        user.setDeviceId(userRequest.getDeviceId());

        return mapper.userToUserDTO(repository.save(user));
    }

    public Boolean checkFollow(Long requesterId, Long requestedId) {
        return repository.checkFollow(requesterId, requestedId) > 0;
    }


    public void updateLastLogin(long userId) {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        user.setLastLogin(LocalDate.now());
        repository.save(user);
    }


    public void delete(User user) {
        repository.delete(user);
    }

    public List<UserDTO> lastWeekUsers() {
        List<User> users = repository.activeUsersInLastWeek(LocalDate.now().minusWeeks(1));
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : users){
            userDTOS.add(mapper.userToUserDTO(user));
        }

        return userDTOS;
    }

    public List<UserDTO> lastDaySignUps() {
        List<User> users = repository.lastDaySignUps(LocalDate.now().minusDays(1));
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : users){
            userDTOS.add(mapper.userToUserDTO(user));
        }

        return userDTOS;
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
