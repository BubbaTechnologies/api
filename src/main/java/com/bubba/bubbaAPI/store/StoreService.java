//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.store;

import lombok.AllArgsConstructor;

import java.util.Optional;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository repository;

    public Store create(Store store) {
        return repository.save(store);
    }

    public Optional<Store> findByUrl(String url) {
        return repository.findByUrl(url);
    }

    public Store disableStore(String storeName) {
        Store store = repository.findByName(storeName).orElseThrow(() -> new StoreNotFoundException(storeName));
        store.setEnabled(false);
        return repository.save(store);
    }


//    public List<Store> getAll(long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/stores");
//        return repository.findAll();
//    }
//
//    public Store create(Store store, Long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/stores --POST");
//
//        return repository.save(store);
//    }
//
//    public Store update(long id, Store storeRequest, long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/stores/{id} --PUT");
//
//        return this.updateStore(id, storeRequest);
//    }
//
//    public void delete(long id, long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/stores/{id} --DELETE");
//
//        Store store = repository.findById(id).orElseThrow(() -> new StoreNotFoundException(id));
//
//        repository.delete(store);
//        clothingService.deleteByStore(store);
//    }
//
//
//    public Store getById(long itemId, long sessionId) {
//        if (!userService.checkAdmin(sessionId))
//            throw new PermissionDeniedException(sessionId, "/stores/{id}");
//        return this.getStore(itemId);
//    }
//
//
//    public Store getStore(long id) {
//        Optional<Store> result = repository.findById(id);
//
//        if (result.isPresent()) {
//            return result.get();
//        } else {
//            throw new StoreNotFoundException(id);
//        }
//    }
//
//    private Store updateStore(long id, Store storeRequest) {
//        Store store = repository.findById(id).orElseThrow(() -> new StoreNotFoundException(id));
//        repository.delete(store);
//
//        store.setId(storeRequest.getId());
//        store.setName(storeRequest.getName());
//        store.setURL(storeRequest.getURL());
//
//        return repository.save(store);
//    }
}
