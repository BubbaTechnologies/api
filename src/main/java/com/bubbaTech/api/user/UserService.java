//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.mapping.Mapper;
import com.bubbaTech.api.security.authorities.Authorities;
import com.bubbaTech.api.user.metricStructs.SessionData;
import com.bubbaTech.api.user.metricStructs.SessionDataDTO;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Mapper mapper;

    private final int topMatches = 10;
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

    public void authUser(String email, String password) throws AuthenticationServiceException {
        UserDTO user = getByEmail(email);
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

    public UserDTO getByEmail(String email) {
        return mapper.userToUserDTO(repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email)));
    }

    /**
     * Gets all device ids.
     * @return: Returns a list of deviceIds.
     */
    public List<String> getAllDeviceIds() {
        List<User> users = repository.findAll();
        List<String> deviceIds = new ArrayList<>();
        for (User user : users) {
            if (user.isEnabled() && user.getDeviceId() != null) {
                deviceIds.add(user.getDeviceId());
            }
        }

        return deviceIds;
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
        requestedUser.setFollowRequests(requests);

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
        if (this.checkUsername(userRequest.getUsername()))
            throw new UserExistsException(userRequest.getUsername());
        if (this.checkEmail(userRequest.getEmail()))
            throw new UserExistsException(userRequest.getEmail());
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
        user.setPrivateAccount(userRequest.getPrivateAccount());
        user.setEmail(userRequest.getEmail());

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

    /**
     * Adds the session data to the database.
     * @param sessionDataDTO: The data being saved to the database.
     */
    public void saveSession(SessionDataDTO sessionDataDTO) {
        Long userId = sessionDataDTO.getUser().getId();

        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        SessionData sessionData = new SessionData();
        sessionData.setSessionLength(sessionDataDTO.getSessionLength());
        sessionData.setDateTimeCreated(sessionDataDTO.getDateTimeCreated());
        sessionData.setUser(user);

        List<SessionData> sessions = user.getSessionData();
        sessions.add(sessionData);
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

    /**
     * Determines the following relation between two users.
     * @param requesterId: The user id of the requester.
     * @param requestedId: The user id of the user being requested.
     * @return: The following status between the two.
     */
    public FollowingStatus getFollowingRelation(Long requesterId, Long requestedId) {
        User requested = repository.getReferenceById(requestedId);

        if (requested.getFollowers().stream().map(User::getId).toList().contains(requesterId)) {
            return FollowingStatus.FOLLOWING;
        } else if (requested.getFollowRequests().stream().map(User::getId).toList().contains(requesterId)) {
            return FollowingStatus.REQUESTED;
        } else {
            return FollowingStatus.NONE;
        }
    }

    /**
     * Returns a list of profiles of the user request to follow the principal.
     * @param userId: UserID of user being queried.
     * @return:
     */
    public List<ProfileDTO> getRequested(Long userId) {
        User requested = repository.getReferenceById(userId);

        List<User> requestedUsers = requested.getFollowRequests();
        List<ProfileDTO> profiles = new ArrayList<>();
        for (User user : requestedUsers) {
            profiles.add(new ProfileDTO(mapper.userToUserDTO(user), getFollowingRelation(userId, user.getId())));
        }

        return profiles;
    }

    /**
     * Finds users with similiar username to query.
     * @param query: A string representing the query.
     * @return: Users with similiar users names to query with a certain threshold of confidence.
     */
    public List<ProfileDTO> searchUsers(String query, Long userId) {
        //Filter special characters and cut query to 60 characters
        if (query.length() > 20) {
            query = query.substring(0, 20);
        }

        List<User> users = repository.findAll();

        //Similarity ranking (smith-waterman)
        List<User> rankedUsers = new ArrayList<>();
        List<Integer> rankUserScores = new ArrayList<>();
        for (User user: users) {
            if (user.getId() > 3 && user.getId() != userId) {
                Integer score = smith_waterman(user.getUsername().toLowerCase(), query.toLowerCase());
                rankedUsers.add(user);
                rankUserScores.add(score);
            }
        }

        //Sorts two lists
        List<User> finalRankedUsers = rankedUsers;
        rankedUsers.sort(Comparator.comparingInt(user -> rankUserScores.get(finalRankedUsers.indexOf(user))).reversed());

        //Return similar users with searchConfidence confidence
        if (rankedUsers.size() > topMatches) {
            rankedUsers = rankedUsers.subList(0, 10);
        }

        List<ProfileDTO> profileDTOS = new ArrayList<>();
        for (User user : rankedUsers) {
            profileDTOS.add(new ProfileDTO(mapper.userToUserDTO(user), getFollowingRelation(userId, user.getId())));
        }


        return profileDTOS;
    }

    /**
     * Determines highest sequence rating between two sequences.
     * @param seq1: String representing first sequence.
     * @param seq2: String representing second sequence.
     * @return: An int representing the sequence alignment score.
     */
    private int smith_waterman(String seq1, String seq2) {
        int matchScore = 4;
        int mismatchPenalty = 6;
        int gapPenalty = 6;

        int maxScore = -1;
        int[][] dp = new int[seq1.length() + 1][seq2.length() + 1];

        for (int i = 1; i <= seq1.length(); i++) {
            for (int j = 1; j < seq2.length(); j++) {
                if (seq1.charAt(i - 1) == seq2.charAt(j - 1)) {
                    dp[i][j] = Math.max(0, dp[i - 1][j - 1] + matchScore);
                } else {
                    dp[i][j] = Math.max(0, Math.max(dp[i - 1][j] - gapPenalty, dp[i][j - 1] - gapPenalty) - mismatchPenalty);
                }
                maxScore = Math.max(maxScore, dp[i][j]);
            }
        }

        return maxScore;
    }
}
