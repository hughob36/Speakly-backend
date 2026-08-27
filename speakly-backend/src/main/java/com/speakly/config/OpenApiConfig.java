package com.speakly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
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