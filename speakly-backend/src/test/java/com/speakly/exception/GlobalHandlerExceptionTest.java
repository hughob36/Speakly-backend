package com.speakly.exception;

import com.speakly.dto.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalHandlerExceptionTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // 1. Empty or null audio file (HTTP 400)
    @Test
    @DisplayName("Should return 400 BAD_REQUEST and original message when EmptyAudioFileException is thrown")
    void handleEmptyAudioFileException_ShouldReturnBadRequest() {
        String customMessage = "The provided audio file is empty or missing.";
        EmptyAudioFileException exception = new EmptyAudioFileException(customMessage);

        ResponseEntity<ErrorResponseDTO> response = handler.handleEmptyAudioFileException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(customMessage, response.getBody().message());
    }

    // 2. Missing multipart request part (MissingServletRequestPartException) (HTTP 400)
    @Test
    @DisplayName("Should return 400 BAD_REQUEST when the required 'file' multipart part is missing")
    void handleMissingFileException_MissingServletRequestPartException_ShouldReturnBadRequest() {
        MissingServletRequestPartException exception = new MissingServletRequestPartException("file");

        ResponseEntity<ErrorResponseDTO> response = handler.handleMissingFileException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Required request part 'file' is missing.", response.getBody().message());
    }

    // 2b. Missing request parameter (MissingServletRequestParameterException) (HTTP 400)
    @Test
    @DisplayName("Should return 400 BAD_REQUEST when a required request parameter is missing")
    void handleMissingFileException_MissingServletRequestParameterException_ShouldReturnBadRequest() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException("file", "MultipartFile");

        ResponseEntity<ErrorResponseDTO> response = handler.handleMissingFileException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Required request part 'file' is missing.", response.getBody().message());
    }

    // 3. Audio file exceeds maximum configured upload size (HTTP 413)
    @Test
    @DisplayName("Should return 413 PAYLOAD_TOO_LARGE when the audio exceeds the configured maximum size")
    void handleMaxUploadSizeExceeded_ShouldReturnPayloadTooLarge() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(10485760L); // 10MB

        ResponseEntity<ErrorResponseDTO> response = handler.handleMaxUploadSizeExceeded(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The audio file exceeds the maximum allowed upload size.", response.getBody().message());
    }

    // 4. External API error response (RestClientResponseException) (HTTP 502)
    @Test
    @DisplayName("Should return 502 BAD_GATEWAY when external AI API responds with an HTTP error")
    void handleExternalApiException_RestClientResponseException_ShouldReturnBadGateway() {
        RestClientResponseException exception = new RestClientResponseException(
                "Unauthorized in external API",
                HttpStatusCode.valueOf(401),
                "Unauthorized",
                HttpHeaders.EMPTY,
                new byte[0],
                null
        );

        ResponseEntity<ErrorResponseDTO> response = handler.handleExternalApiException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AI service communication error. Please try again later.", response.getBody().message());
    }

    // 4b. Network timeout or connection failure with external API (ResourceAccessException) (HTTP 502)
    @Test
    @DisplayName("Should return 502 BAD_GATEWAY when external AI API experiences a connection failure or timeout")
    void handleExternalApiException_ResourceAccessException_ShouldReturnBadGateway() {
        ResourceAccessException exception = new ResourceAccessException("Connection timed out");

        ResponseEntity<ErrorResponseDTO> response = handler.handleExternalApiException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AI service communication error. Please try again later.", response.getBody().message());
    }

    // 5. Fallback for unhandled exceptions (HTTP 500)
    @Test
    @DisplayName("Should return 500 INTERNAL_SERVER_ERROR when an unhandled generic exception occurs")
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new NullPointerException("Simulated unexpected null reference");

        ResponseEntity<ErrorResponseDTO> response = handler.handleGenericException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred on the server.", response.getBody().message());
    }
}