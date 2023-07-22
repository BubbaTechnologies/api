//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.store;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository repository;
    private final ModelMapper modelMapper;

    public StoreDTO create(StoreDTO store) {
        //Return store if it is found
        if (this.checkByUrl(store.getURL())) {
            Store foundStore = repository.findByUrl(store.getURL()).get();
            return modelMapper.map(foundStore, StoreDTO.class);
        }
        store.setEnabled(true);
        Store newStore = new Store(store);
        return modelMapper.map(repository.save(newStore),StoreDTO.class);
    }

    public Boolean checkByUrl(String url) {
        return repository.findByUrl(url).isPresent();
    }

    public Optional<StoreDTO> getByUrl(String url) {
        if (!this.checkByUrl(url))
            return Optional.empty();
        Store store = repository.findByUrl(url).get();
        return Optional.of(modelMapper.map(store, StoreDTO.class));
    }

    public StoreDTO disableStore(String storeName) {
        Store store = repository.findByName(storeName).orElseThrow(() -> new StoreNotFoundException(storeName));
        store.setEnabled(false);
        return modelMapper.map(repository.save(store), StoreDTO.class);
    }

    public List<StoreDTO> getAll() {
        List<Store> stores = repository.findAll();
        List<StoreDTO> storeDTOS = new ArrayList<>();
        for (Store store : stores) {
            storeDTOS.add(modelMapper.map(store, StoreDTO.class));
        }
        return storeDTOS;
    }
}
