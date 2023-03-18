package com.sternitc.x509server;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Configuration
public class WebClientConfiguration {

    private static final String PASSWORD = "changeit";

    @Bean
    public SslContext sslContext() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        try (InputStream file = new ClassPathResource("ssl/keystore.p12").getInputStream()) {
            final KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(file, PASSWORD.toCharArray());
            keyManagerFactory.init(keyStore, PASSWORD.toCharArray());
        }

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        try (InputStream trustStoreFile = new ClassPathResource("ssl/truststore.jks").getInputStream()) {
            final KeyStore trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(trustStoreFile, PASSWORD.toCharArray());
            trustManagerFactory.init(trustStore);
        }

        return SslContextBuilder.forClient()
                .keyManager(keyManagerFactory).trustManager(trustManagerFactory).build();
    }

    @Bean
    public WebClient webClient(SslContext sslContext) {
        SslProvider sslProvider = SslProvider.builder()
                .sslContext(sslContext).build();
        reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create()
                .secure(sslProvider);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

}
