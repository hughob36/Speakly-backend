package com.speakly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();

        Server prodServer = new Server();
        prodServer.setUrl("https://speakly-backend.duckdns.org");
        prodServer.setDescription("Producción VPS (HTTPS)");
        servers.add(prodServer);

        return new OpenAPI()
                .servers(servers)
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