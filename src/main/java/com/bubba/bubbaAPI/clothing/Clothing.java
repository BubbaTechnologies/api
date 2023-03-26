//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.clothing;

import com.bubba.bubbaAPI.like.Like;
import com.bubba.bubbaAPI.medianGroup.MedianGroup;
import com.bubba.bubbaAPI.store.Store;
import com.bubba.bubbaAPI.user.Gender;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
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
    private String imageURL;
    private String productURL;
    @ManyToMany()
    @JoinTable(name = "clothing_group",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "clothing_id"))
    @JsonManagedReference
    private List<MedianGroup> groups;

    @ManyToOne()
    @JoinColumn(name = "store_id")
    @JsonManagedReference
    private Store store;

    @OneToMany()
    @JoinColumn(name = "like_id")
    @JsonManagedReference
    private List<Like> likes;

    private ClothType type;

    @ElementCollection(fetch = FetchType.LAZY)
    private Collection<Gender> gender;

    protected Clothing() {
    }

    public Clothing(String name, String imageURL, String productURL, Store store, ClothType type, Collection<Gender> gender) {
        this.name = name;
        this.imageURL = imageURL;
        this.productURL = productURL;
        this.store = store;
        this.type = type;
        this.gender = gender;
        this.groups = null;
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
                && Objects.equals(this.productURL, clothing.productURL) && Objects.equals(this.store, clothing.store) && this.type == clothing.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.imageURL, this.productURL, this.store, this.type);
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder("Clothing{" + "id=" + this.id + ", name='" + this.name + '\'' + ", imageURL='" + this.imageURL + '\'' + ", productURL='" + this.productURL + '\'' + ", store=" + this.store + ", type=" + this.type + ", groups=");
        for (MedianGroup group : this.groups) {
            returnString.append(group.toStringBasic());
        }

        returnString.append('}');
        return returnString.toString();
    }

    public String toStringBasic() {
        return "Clothing{" + "id=" + this.id + ", name='" + this.name + '\'' + ", imageURL='" + this.imageURL + '\'' + ", productURL='" + this.productURL + '\'' + ", store=" + this.store + ", type=" + this.type + '}';
    }
}