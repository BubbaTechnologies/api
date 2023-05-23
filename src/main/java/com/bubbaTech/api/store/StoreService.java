//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.store;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository repository;

    public Store create(Store store) {
        Optional<Store> returnObj = this.findByUrl(store.getURL());
        if (returnObj.isPresent())
            return returnObj.get();
        store.setEnabled(true);
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

    public List<Store> getAll() {
        return repository.findAll();
    }
}
