//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.store;

import com.bubba.bubbaAPI.clothing.Clothing;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Table(name = "STORE")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "URL")
    private String URL;

    @OneToMany(mappedBy = "store", fetch = FetchType.EAGER)
    @JsonBackReference
    private List<Clothing> items;

    private boolean enabled;

    public Store() {
    }

    public Store(Long id) {
    }

    public Store(String name, String URL) {
        this.name = name;
        this.URL = URL;
        this.items = new ArrayList<>();
        this.enabled = true;
    }

    //Overrides
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Store))
            return false;

        Store store = (Store) o;
        return Objects.equals(this.id, store.id) && Objects.equals(this.name, store.name) && Objects.equals(this.URL, store.URL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.URL);
    }

    @Override
    public String toString() {
        return "store{" + "id=" + this.id + ", name='" + this.name + '\'' + ", URL='" + this.URL + '\'' + '}';
    }
}