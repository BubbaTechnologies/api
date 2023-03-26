//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.medianGroup;

import com.bubba.bubbaAPI.clothing.Clothing;
import com.bubba.bubbaAPI.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Table(name = "MEDIAN_GROUP")
public class MedianGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "medianGroup", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<User> users;

    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.TRUE)
    @Column(name = "items")
    @JsonBackReference
    private List<Clothing> items;

    public MedianGroup() {
    }


    public String toStringBasic() {
        return "MedianGroup{" + "id=" + this.id + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof MedianGroup))
            return false;

        MedianGroup medianGroup = (MedianGroup) o;

        return Objects.equals(this.id, medianGroup.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder("MedianGroup{" + "id=" + this.id + ", users=");
        for (User user : this.users) {
            returnString.append(user.toStringBasic());
        }

        returnString.append(", items=");

//        for (Clothing item : this.items) {
//            returnString.append(item.toStringBasic());
//        }

        returnString.append('}');

        return returnString.toString();
    }
}