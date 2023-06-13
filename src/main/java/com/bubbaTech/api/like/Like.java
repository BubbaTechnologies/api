//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.like;

import com.bubbaTech.api.clothing.Clothing;
import com.bubbaTech.api.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "LIKES")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JsonBackReference
    private User user;

    @ManyToOne()
    @JsonBackReference
    private Clothing clothing;

    private int rating;

    @Column(name="date_updated")
    private LocalDateTime date;

    private boolean liked;

    private boolean bought;

    public Like() {
    }

    Like(User user, Clothing clothing) {
        this.user = user;
        this.clothing = clothing;
    }

    @PrePersist
    public void prePersist() {
        this.date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    @PreUpdate
    public void preUpdate() {
        this.date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public String toString() {
        return "Like{" + "id=" + this.id + "user=" + this.user + ", clothing=" + this.clothing + ", rating=" + this.rating + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Like like = (Like) o;
        return getId() != null && Objects.equals(getId(), like.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}