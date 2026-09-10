package com.speakly;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@OpenAPIDefinition(servers = {
		@Server(url = "/", description = "Servidor Principal (HTTPS / Local)")
})
@SpringBootTest
class SpeaklyBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
