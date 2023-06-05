//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.like.Like;
import com.bubbaTech.api.security.authorities.Authorities;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "USERS")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @OneToMany()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonManagedReference
    private List<Like> likes;

    @Column(name = "gender")
    private Gender gender;

    @ElementCollection(fetch = FetchType.EAGER)
    private Collection<Authorities> grantedAuthorities;

    private Boolean locked;

    private Date accountExpiration;

    private Date credentialExpiration;

    private Boolean enabled;

    private LocalDate accountCreated;

    private LocalDate lastLogin;

    //Constructors
    protected User() {
    }

    public User(String email, String password, Gender gender) {
        this.password = password;
        this.gender = gender;
        this.username = email;
        this.likes = null;
        this.grantedAuthorities = null;
        this.enabled = true;
    }

    public User(String email, String password, Gender gender, Collection<Authorities> authorities, String name) {
        this.password = password;
        this.gender = gender;
        this.username = email;
        this.likes = null;
        this.grantedAuthorities = authorities;
        this.enabled = true;
        this.name = name;
    }

    @PrePersist
    public void prePersist() {
        this.accountCreated = LocalDate.now();
        this.lastLogin = LocalDate.now();
    }

    //Overrides
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;

        User user = (User) o;
        return Objects.equals(this.id, user.id) && Objects.equals(this.password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.password);
    }

    @Override
    public String toString() {
        return "User{" + "id=" + this.id + ", username='" + this.username + '\'' + '}';
    }

    public String toStringBasic() {
        return "User{" + "id=" + this.id + ", username='" + this.username + '\'' + ", name='" + this.name + '\'' + '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        //TODO
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        //TODO
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        //TODO
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}