package com.speakly.exception;

import com.speakly.dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j //creo el log automatico con lombok
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Manejo de archivo de audio inválido o vacío (HTTP 400)
    @ExceptionHandler(EmptyAudioFileException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmptyAudioFileException(EmptyAudioFileException ex) {
        log.warn("Solicitud inválida de audio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(ex.getMessage()));
    }

    //este lanza el error 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        // El más importante: nos avisa de lo que no previmos
        log.error("ERROR CRÍTICO NO CONTROLADO: ", ex);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO("An unexpected error occurred on the server.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
