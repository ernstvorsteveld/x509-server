package com.sternitc.x509server;

import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.JettySslUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collections;

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
        return cn -> {
            if (cn.equals("localhost1")) {
                return new User(cn, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_User, ROLE_Admin"));
            } else if (cn.equals("localhost2")) {
                return new User(cn, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_Admin"));
            } else {
                throw new UsernameNotFoundException("User not found: " + cn);
            }
        };
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(SslContextFactory.Server sslContextFactory) {
        JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
        JettyServerCustomizer jettyServerCustomizer = server -> {
            ServerConnector serverConnector = new ServerConnector(server, sslContextFactory);
            serverConnector.setPort(8443);
            server.setConnectors(new Connector[]{serverConnector});
        };
        factory.setServerCustomizers(Collections.singletonList(jettyServerCustomizer));
        return factory;
    }

    @Bean
    public SSLFactory sslFactory() {
        return SSLFactory.builder()
                .withSwappableIdentityMaterial()
                .withIdentityMaterial("ssl/keystore.p12", "changeit".toCharArray())
                .withSwappableTrustMaterial()
                .withTrustMaterial("ssl/truststore.jks", "changeit".toCharArray())
                .withNeedClientAuthentication()
                .build();
    }

    @Bean
    public SslContextFactory.Server sslContextFactory(SSLFactory sslFactory) {
        return JettySslUtils.forServer(sslFactory);
    }
}
