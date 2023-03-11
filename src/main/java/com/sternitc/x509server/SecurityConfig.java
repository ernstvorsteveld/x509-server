package com.sternitc.x509server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher("/api/hello")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/secured")).hasRole("User")
                .requestMatchers(new AntPathRequestMatcher("/api/secured/**")).hasRole("User")
                .requestMatchers(new AntPathRequestMatcher("/api/admin")).hasRole("Admin")
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasRole("Admin")
                .and().x509().subjectPrincipalRegex("CN=(.*?),")
                .userDetailsService(userDetailsService());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return (UserDetailsService) cn -> {
            if (cn.equals("localhost1")) {
                return new User(cn, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_User, ROLE_Admin"));
            } else if (cn.equals("localhost2")) {
                return new User(cn, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_Admin"));
            } else {
                throw new UsernameNotFoundException("User not found: " + cn);
            }
        };
    }
}
