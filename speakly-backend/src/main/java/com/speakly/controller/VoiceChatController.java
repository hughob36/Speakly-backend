package com.speakly.controller;

import com.speakly.dto.ErrorResponseDTO;
import com.speakly.dto.TutorResponseDTO;
import com.speakly.service.VoiceChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Speakly AI - Voice Chat", description = "Endpoints de interacción por voz y procesamiento de audio con el tutor de IA")
@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VoiceChatController {

    private final VoiceChatService voiceChatService;

    @Operation(
            summary = "Procesar audio del usuario y obtener respuesta del tutor",
            description = "Recibe un archivo de audio en formato multipart/form-data, lo transcribe con Whisper, genera la corrección/respuesta con el LLM y devuelve el feedback estructurado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Audio procesado con éxito y respuesta generada",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TutorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Archivo de audio vacío o parámetro 'file' ausente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "El archivo excede el tamaño máximo permitido",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Error de comunicación o timeout con el proveedor de IA (Groq / OpenAI)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno no controlado en el servidor",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping(value = "/talk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TutorResponseDTO> talk(
            @Parameter(
                    description = "Archivo de audio grabado por el usuario (wav, mp3, ogg, webm, m4a)",
                    required = true
            )
            @RequestParam("file") MultipartFile audioFile) {

        TutorResponseDTO tutorReply = voiceChatService.processVoiceConversation(audioFile);
        return ResponseEntity.ok(tutorReply);
    }
}