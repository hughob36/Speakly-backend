package com.speakly.exception;

import com.speakly.dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Audio file empty or missing based on service validation (HTTP 400)
    @ExceptionHandler(EmptyAudioFileException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmptyAudioFileException(EmptyAudioFileException ex) {
        log.warn("Invalid audio request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    // 2. Missing required multipart part 'file' (HTTP 400)
    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleMissingFileException(Exception ex) {
        log.warn("Required audio file is missing from request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Required request part 'file' is missing."));
    }

    // 3. Audio file exceeds maximum configured upload size (HTTP 413)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("Audio file exceeds maximum allowed upload size: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponseDTO("The audio file exceeds the maximum allowed upload size."));
    }

    // 4. External AI API error response (401, 429, 500, etc.) or network timeout (HTTP 502)
    @ExceptionHandler({RestClientResponseException.class, ResourceAccessException.class})
    public ResponseEntity<ErrorResponseDTO> handleExternalApiException(Exception ex) {
        log.error("Error communicating with AI API: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponseDTO("AI service communication error. Please try again later."));
    }

    // 5. Fallback for unhandled critical errors (HTTP 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("CRITICAL UNHANDLED ERROR: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("An unexpected error occurred on the server."));
    }
}