//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.store;

import com.bubbaTech.api.mapping.Mapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository repository;
    private final Mapper mapper;

    public StoreDTO create(StoreDTO store) {
        //Return store if it is found
        if (this.checkByUrl(store.getURL())) {
            Store foundStore = repository.findByUrl(store.getURL()).get();
            return mapper.storeToStoreDTO(foundStore);
        }
        store.setEnabled(true);
        Store newStore = new Store(store);
        return mapper.storeToStoreDTO(repository.save(newStore));
    }

    public Boolean checkByUrl(String url) {
        return repository.findByUrl(url).isPresent();
    }

    public Optional<StoreDTO> getByUrl(String url) {
        Optional<Store> store = repository.findByUrl(url);
        return store.map(mapper::storeToStoreDTO);
    }

    public StoreDTO disableStore(String storeName) {
        Store store = repository.findByName(storeName).orElseThrow(() -> new StoreNotFoundException(storeName));
        store.setEnabled(false);
        return mapper.storeToStoreDTO(repository.save(store));
    }

    public List<StoreDTO> getAll() {
        List<Store> stores = repository.findAll();
        List<StoreDTO> storeDTOS = new ArrayList<>();
        for (Store store : stores) {
            storeDTOS.add(mapper.storeToStoreDTO(store));
        }
        return storeDTOS;
    }
}
