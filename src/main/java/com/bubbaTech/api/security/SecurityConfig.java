//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.security;

import com.bubbaTech.api.security.authentication.CustomAuthenticationManager;
import com.bubbaTech.api.security.authentication.CustomUserDetailsService;
import com.bubbaTech.api.security.authentication.JwtRequestFilter;
import com.bubbaTech.api.security.authentication.UpAuthenticationProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class SecurityConfig {
    @NonNull
    private CustomUserDetailsService customUserDetailsService;
    @NonNull
    private UpAuthenticationProvider upAuthenticationProvider;
    @NonNull
    private JwtRequestFilter jwtFilter;

    @Value("${spring.datasource.url}")
    private String serverAddress;
    @Value("${spring.datasource.username}")
    private String serverUsername;
    @Value("${spring.datasource.password}")
    private String serverPassword;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(serverAddress);
        dataSource.setUsername(serverUsername);
        dataSource.setPassword(serverPassword);
        return dataSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                        .requestMatchers("/ai/**").hasAuthority("AI")
                        .requestMatchers("/app/**").hasAuthority("USER")
                        .requestMatchers("/scraper/**").hasAuthority("SCRAPER")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/", "/create", "/login", "/health", "/logout").permitAll()
                        .anyRequest().authenticated())
                .authenticationManager(new CustomAuthenticationManager())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public UserDetailsManager users (DataSource dataSource, PasswordEncoder passwordEncoder) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        JdbcUserDetailsManagerConfigurer<?> config = new JdbcUserDetailsManagerConfigurer<>(users);
        config.passwordEncoder(passwordEncoder);
        return users;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 16, new java.security.SecureRandom());
    }
}

