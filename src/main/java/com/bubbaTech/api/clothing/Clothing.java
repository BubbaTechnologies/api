//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.errorLogging.clothingError.ClothingError;
import com.bubbaTech.api.like.Like;
import com.bubbaTech.api.store.Store;
import com.bubbaTech.api.user.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.PERSIST;

@Entity
@Getter
@Setter
@Table(name = "CLOTHING")
public class Clothing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private String name;
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> imageURL;
    private String productURL;
    @ManyToOne()
    @JoinColumn(name = "store_id")
    private Store store;
    @OneToMany()
    @JoinColumn(name = "like_ids")
    @ToString.Exclude
    private List<Like> likes;
    private ClothType clothingType;
    private Gender gender;
    @ElementCollection(fetch = FetchType.LAZY)
    private List<ClothingTag> tags;
    @Column(name = "date_created")
    private LocalDate date;

    private Boolean enabled;

    @OneToMany(mappedBy = "clothing", cascade={PERSIST, DETACH})
    private List<ClothingError> errors;

    public Clothing() {}

    public Clothing(String name, List<String> imageURL, String productURL, Store store, ClothType type, Gender gender, List<ClothingTag> tags) {
        this.name = name;
        this.imageURL = imageURL;
        this.productURL = productURL;
        this.store = store;
        this.clothingType = type;
        this.gender = gender;
        this.tags = tags;
        this.enabled = true;
    }

    //Sets date before saving
    @PrePersist
    public void prePersist() {
        if (enabled == null) {
            enabled = true;
        }
        this.date = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.date = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Clothing clothing = (Clothing) o;
        return getId() != null && Objects.equals(getId(), clothing.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}