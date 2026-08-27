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

    // 1. Audio vacío o nulo según la validación de tu servicio (HTTP 400)
    @ExceptionHandler(EmptyAudioFileException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmptyAudioFileException(EmptyAudioFileException ex) {
        log.warn("Solicitud inválida de audio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    // 2. No se adjuntó el campo multipart requerido "file" (HTTP 400)
    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleMissingFileException(Exception ex) {
        log.warn("Falta el archivo de audio requerido en la petición: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Required request part 'file' is missing."));
    }

    // 3. El archivo supera el límite configurado en application.properties (HTTP 413)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("El archivo de audio supera el tamaño máximo permitido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponseDTO("The audio file exceeds the maximum allowed upload size."));
    }

    // 4. Errores directos devueltos por la API de Groq/OpenAI (401, 429, 500, etc.) o timeout de red (HTTP 502)
    @ExceptionHandler({RestClientResponseException.class, ResourceAccessException.class})
    public ResponseEntity<ErrorResponseDTO> handleExternalApiException(Exception ex) {
        log.error("Error al comunicar con la API de IA: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponseDTO("AI service communication error. Please try again later."));
    }

    // 5. Fallback para errores no controlados (HTTP 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("ERROR CRÍTICO NO CONTROLADO: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("An unexpected error occurred on the server."));
    }
}