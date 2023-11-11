//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.like.Like;
import com.bubbaTech.api.security.authorities.Authorities;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "USERS")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "password")
    private String password;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<Like> likes;
    @Column(name = "gender")
    private Gender gender;
    @ElementCollection(fetch = FetchType.EAGER)
    private Collection<Authorities> grantedAuthorities;
    private Boolean enabled;
    private LocalDate accountCreated;
    private LocalDate lastLogin;
    private LocalDate birthDate;
    @Nullable
    @Column(name = "latitude")
    private Double latitude;
    @Nullable
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "deviceId")
    private String deviceId;

    //Constructors
    public User() {
    }

    public User(String email, String password, Gender gender) {
        this.password = password;
        this.gender = gender;
        this.username = email;
        this.likes = null;
        this.grantedAuthorities = null;
        this.enabled = true;
    }

    public User(String email, String password, Gender gender, Collection<Authorities> authorities) {
        this.password = password;
        this.gender = gender;
        this.username = email;
        this.likes = null;
        this.grantedAuthorities = authorities;
        this.enabled = true;
    }

    @PrePersist
    public void prePersist() {
        this.accountCreated = LocalDate.now();
        this.lastLogin = LocalDate.now();
    }

    @Override
    public String toString() {
        return "User{" + "id=" + this.id + ", username='" + this.username + '\'' + '}';
    }

    public String toStringBasic() {
        return "User{" + "id=" + this.id + ", username='" + this.username + '\'' + '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}