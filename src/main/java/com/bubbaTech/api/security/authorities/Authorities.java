//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.security.authorities;

import jakarta.persistence.Embeddable;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Embeddable
@Data
public class Authorities implements GrantedAuthority {
    public Authorities() {}

    private enum authoritiesType {
        USER("USER"), AI("AI"), SCRAPER("SCRAPER"), ADMIN("ADMIN");

        private final String value;

        authoritiesType(String value) {
            this.value = value;
        }

        public String getString() {
            return this.value;
        }
    }

    private authoritiesType authority;

    public Authorities(String authority) {
        this.authority = authoritiesType.valueOf(authority);
    }

    @Override
    public String getAuthority() {
        return this.authority.getString();
    }

    public void setAuthority(String authority) {
        this.authority = authoritiesType.valueOf(authority);
    }
}
