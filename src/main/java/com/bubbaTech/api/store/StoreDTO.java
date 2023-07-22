//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.store;

import com.bubbaTech.api.generic.DTO;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
@JsonDeserialize(using = StoreDeserializer.class)
public class StoreDTO implements DTO<StoreDTO> {
    private Long id;
    private String name;
    private String URL;
    private boolean enabled;

    public StoreDTO() {}

    public StoreDTO(long id) {
        this.id = id;
    }

    public StoreDTO(String name, String URL) {
        this.name = name;
        this.URL = URL;
        this.enabled = true;
    }

    public StoreDTO(String name, String URL, boolean enabled) {
        this.name = name;
        this.URL = URL;
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "Store{" + "id=" + this.id + ", name='" + this.name + '\'' + ", URL='" + this.URL + '\'' + '}';
    }
}
