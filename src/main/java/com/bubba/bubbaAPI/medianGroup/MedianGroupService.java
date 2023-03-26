//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.medianGroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedianGroupService {
    private final MedianGroupRepository repository;

//    public List<MedianGroup> getAll(long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/groups");
//
//        return repository.findAll();
//    }
//
//    public MedianGroup create(MedianGroup medianGroup, long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/groups --POST");
//
//        return this.repository.save(medianGroup);
//    }
//
//    public MedianGroup update(long id, MedianGroup groupRequest, long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/groups");
//
//        MedianGroup group = repository.findById(id).orElseThrow(() -> new MedianGroupNotFoundException(id));
//
//        group.setUsers(groupRequest.getUsers());
//        group.setItems(groupRequest.getItems());
//        group.setId(groupRequest.getId());
//
//        return repository.save(group);
//    }
//
//
//    public void delete(long id, long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/groups");
//
//        MedianGroup group = repository.findById(id).orElseThrow(() -> new MedianGroupNotFoundException(id));
//        repository.delete(group);
//    }
//
//
//    public MedianGroup getById(long id, long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/groups");
//
//        return this.getGroup(id);
//    }
//
//    void removeUser(long userId) {
//        MedianGroup group = repository.getWithUser(userId);
//
//        User user = getUser(userId);
//        List<User> users = group.getUsers();
//        users.remove(user);
//        group.setUsers(users);
//        repository.save(group);
//    }
//
//    void removeItem(long itemId) {
//        List<MedianGroup> groups = repository.getAllWithItem(itemId);
//
//        Clothing item = getItem(itemId);
//        for (MedianGroup group : groups) {
//            List<Clothing> items = group.getItems();
//            items.remove(item);
//            group.setItems(items);
//            repository.save(group);
//        }
//    }
//
//    public User getUser(long userId) {
//        return userService.getUser(userId);
//    }
//
//    public Clothing getItem(long itemId) {
//        return clothingService.getItem(itemId);
//    }
//
//    private MedianGroup getGroup(long id) {
//        Optional<MedianGroup> result = repository.findById(id);
//
//        if (result.isPresent())
//            return result.get();
//        else
//            throw new MedianGroupNotFoundException(id);
//    }
}
