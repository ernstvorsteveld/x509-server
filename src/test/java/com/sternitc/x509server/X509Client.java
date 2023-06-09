package com.sternitc.x509server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@Import(WebClientConfiguration.class)
//@Configuration
public class X509Client {

    @Autowired
    private WebClient webClient;

    @Test
    public void should_authorize_localhost1_on_secured_url() {
        String result = webClient
                .get()
                .uri("https://localhost:8443/api/admin")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(result).isEqualTo("Admin content!");
    }
}
