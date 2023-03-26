//Matthew Grohoslki
//Bubba Technologies Inc.
//10/01/2022

package com.bubba.bubbaAPI.security;

import com.bubba.bubbaAPI.security.authentication.CustomUserDetailsService;
import com.bubba.bubbaAPI.security.authentication.JwtRequestFilter;
import com.bubba.bubbaAPI.security.authentication.UPAuthenticationProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.JdbcUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
public class securityConfig {
    @NonNull
    private CustomUserDetailsService customUserDetailsService;
    @NonNull
    private UPAuthenticationProvider upAuthenticationProvider;
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
        http.csrf().disable()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests((auth) -> {
                    try {
                        auth.antMatchers("/ai/**").hasAuthority("AI")
                                .antMatchers("/app/**").hasAuthority("USER")
                                .antMatchers("/scraper/**").hasAuthority("SCRAPER")
                                .antMatchers("/admin/**").hasAuthority("ADMIN")
                                .antMatchers("/", "/create", "/login", "/health", "/logout").permitAll()
                                .anyRequest().authenticated()
                                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                .and().formLogin().disable()
                                .logout().permitAll();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        return http.build();
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

    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
        authenticationManagerBuilder.authenticationProvider(upAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }
}
