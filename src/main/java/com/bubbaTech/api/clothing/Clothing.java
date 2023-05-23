//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.clothing;

import com.bubbaTech.api.like.Like;
import com.bubbaTech.api.store.Store;
import com.bubbaTech.api.user.Gender;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Table(name = "CLOTHING")
public class Clothing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    private Collection<String> imageURL;

    private String productURL;

    @ManyToOne()
    @JoinColumn(name = "store_id")
    @JsonManagedReference
    private Store store;

    @OneToMany()
    @JoinColumn(name = "id")
    @JsonManagedReference
    private List<Like> likes;

    private ClothType clothingType;

    private Gender gender;

    @Column(name = "date_created")
    private LocalDate date;

    protected Clothing() {
    }

    public Clothing(String name, Collection<String> imageURL, String productURL, Store store, ClothType type, Gender gender) {
        this.name = name;
        this.imageURL = imageURL;
        this.productURL = productURL;
        this.store = store;
        this.clothingType = type;
        this.gender = gender;
    }

    //Sets date before saving
    @PrePersist
    public void prePersist() {
        this.date = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.date = LocalDate.now();
    }

    //Overrides
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Clothing))
            return false;
        Clothing clothing = (Clothing) o;
        return Objects.equals(this.id, clothing.id) && Objects.equals(this.name, clothing.name) && Objects.equals(this.imageURL, clothing.imageURL)
                && Objects.equals(this.productURL, clothing.productURL) && Objects.equals(this.store, clothing.store) && this.clothingType == clothing.clothingType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.imageURL, this.productURL, this.store, this.clothingType);
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder("Clothing{" + "id=" + this.id + ", name='" + this.name + '\'' + ", productURL='" + this.productURL + '\'' + ", store=" + this.store + ", type=" + this.clothingType);
        returnString.append('}');
        return returnString.toString();
    }

    public String toStringBasic() {
        return "Clothing{" + "id=" + this.id + ", name='" + this.name + '\'' +  ", productURL='" + this.productURL + '\'' + ", store=" + this.store + ", type=" + this.clothingType + '}';
    }
}