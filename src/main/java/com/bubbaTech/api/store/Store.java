//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.store;

import com.bubbaTech.api.clothing.Clothing;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
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

    public Store(String name, String URL) {
        this.name = name;
        this.URL = URL;
        this.enabled = true;
    }

    public Store(StoreDTO storeDTO) {
        this.name = storeDTO.getName();
        this.URL = storeDTO.getURL();
        this.enabled = storeDTO.isEnabled();
    }

    @Override
    public String toString() {
        return "store{" + "id=" + this.id + ", name='" + this.name + '\'' + ", URL='" + this.URL + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Store store = (Store) o;
        return getId() != null && Objects.equals(getId(), store.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}