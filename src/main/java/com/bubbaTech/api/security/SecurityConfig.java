//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api.security;

import com.bubbaTech.api.security.authentication.CustomUserDetailsService;
import com.bubbaTech.api.security.authentication.JwtRequestFilter;
import com.bubbaTech.api.security.authentication.UpAuthenticationProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class SecurityConfig {
    @NonNull private CustomUserDetailsService customUserDetailsService;
    @NonNull private UpAuthenticationProvider upAuthenticationProvider;
    @NonNull private JwtRequestFilter jwtFilter;

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
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/create", "/login", "/health", "/logout", "/app/image", "/app/filterOptions", "/verify", "/browsing").permitAll()
                        .requestMatchers("/ai/**").hasAuthority("AI")
                        .requestMatchers("/app/**").hasAuthority("USER")
                        .requestMatchers("/scraper/**").hasAuthority("SCRAPER")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/actuator/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(@NonNull HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
        authenticationManagerBuilder.authenticationProvider(upAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public UserDetailsManager users(DataSource dataSource, PasswordEncoder passwordEncoder) {
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

