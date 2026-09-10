package com.speakly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server prodServer = new Server();
        prodServer.setUrl("https://speakly-backend.duckdns.org");
        prodServer.setDescription("Producción VPS (OCI - HTTPS)");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Entorno Local");

        return new OpenAPI()
                .servers(List.of(prodServer, localServer))
                .info(new Info()
                        .title("Speakly AI - Voice API")
                        .version("1.0.0")
                        .description("API backend para la interacción conversacional por voz y feedback en tiempo real con IA.")
                        .contact(new Contact()
                                .name("Hugo Benitez")
                                .url("https://hughob36.github.io/Porfolio/"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}