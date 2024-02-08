//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.user;

import com.bubbaTech.api.like.Like;
import com.bubbaTech.api.security.authorities.Authorities;
import com.bubbaTech.api.user.metricStructs.SessionData;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.PERSIST;

@Getter
@Setter
@Entity
@Table(name = "USERS")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //Display name
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "password")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private List<Like> likes;

    //Social Connections
    @ManyToMany(cascade={PERSIST, DETACH})
    @JoinTable(
            name = "user_following",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private List<User> following;

    @ManyToMany(mappedBy = "following")
    private List<User> followers;

    @ManyToMany(cascade={PERSIST, DETACH})
    @JoinTable(
            name = "follow_requests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "request_user_id")
    )
    private List<User> followRequests;

    //Authorities within API
    @ElementCollection(fetch = FetchType.EAGER)
    private Collection<Authorities> grantedAuthorities;

    //Determines if account is active
    private Boolean enabled;

    //Account Information
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "gender")
    private Gender gender;
    private LocalDate accountCreated;
    private LocalDate lastLogin;
    @Nullable
    private LocalDate birthDate;
    private Boolean privateAccount;

    @OneToMany(mappedBy = "user", cascade={PERSIST, DETACH})
    private List<SessionData> sessionData;

    //Device location
    @Nullable
    @Column(name = "latitude")
    private Double latitude;
    @Nullable
    @Column(name = "longitude")
    private Double longitude;

    //Notifications for device
    @Nullable
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
        if (this.enabled == null) {
            this.enabled = true;
        }

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