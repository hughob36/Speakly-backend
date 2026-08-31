package com.speakly.controller;

import com.speakly.dto.TutorResponseDTO;
import com.speakly.exception.EmptyAudioFileException;
import com.speakly.exception.GlobalExceptionHandler;
import com.speakly.service.VoiceChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VoiceChatController.class)
@Import(GlobalExceptionHandler.class)
class VoiceChatControllerIntTest {

    private static final String BASE_URL = "/api/voice/talk";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VoiceChatService voiceChatService;

    @Nested
    @DisplayName("Casos Positivos (2xx)")
    class PositiveCases {

        @Test
        @DisplayName("Debe retornar 200 OK con la transcripción y feedback cuando el audio es válido")
        void talk_WithValidAudio_ShouldReturn200AndTutorResponse() throws Exception {
            // Given
            MockMultipartFile validAudioFile = new MockMultipartFile(
                    "file",
                    "voice.wav",
                    "audio/wav",
                    "dummy audio content bytes".getBytes()
            );

            TutorResponseDTO mockResponse = new TutorResponseDTO(
                    "I goes to school yesterday.",
                    "You should say 'I went to school yesterday' instead of 'goes'. What did you learn there? Tell me more!"
            );

            given(voiceChatService.processVoiceConversation(any())).willReturn(mockResponse);

            // When & Then
            mockMvc.perform(multipart(BASE_URL)
                            .file(validAudioFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userSaid").value("I goes to school yesterday."))
                    .andExpect(jsonPath("$.tutorReply").value("You should say 'I went to school yesterday' instead of 'goes'. What did you learn there? Tell me more!"));
        }

        @Test
        @DisplayName("Debe retornar 200 OK con mensaje de fallback cuando el audio es inaudible o silencioso")
        void talk_WithInaudibleAudio_ShouldReturn200AndFallbackMessage() throws Exception {
            // Given
            MockMultipartFile silentAudioFile = new MockMultipartFile(
                    "file",
                    "silence.wav",
                    "audio/wav",
                    new byte[]{0, 0, 0, 0}
            );

            TutorResponseDTO fallbackResponse = new TutorResponseDTO(
                    "",
                    "Could you repeat that? I couldn't hear you."
            );

            given(voiceChatService.processVoiceConversation(any())).willReturn(fallbackResponse);

            // When & Then
            mockMvc.perform(multipart(BASE_URL)
                            .file(silentAudioFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userSaid").value(""))
                    .andExpect(jsonPath("$.tutorReply").value("Could you repeat that? I couldn't hear you."));
        }
    }

    @Nested
    @DisplayName("Casos Negativos (4xx y 5xx)")
    class NegativeCases {

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando el archivo está vacío (EmptyAudioFileException)")
        void talk_WithEmptyAudioFile_ShouldReturn400BadRequest() throws Exception {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.wav",
                    "audio/wav",
                    new byte[0]
            );

            String errorMessage = "The provided audio file is empty or missing.";
            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new EmptyAudioFileException(errorMessage));

            // When & Then
            mockMvc.perform(multipart(BASE_URL)
                            .file(emptyFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando no se envía la parte 'file' requerida")
        void talk_MissingFileParam_ShouldReturn400BadRequest() throws Exception {
            // Given: se envía un archivo con nombre de parámetro incorrecto ("audio" en lugar de "file")
            MockMultipartFile wrongPartFile = new MockMultipartFile(
                    "audio",
                    "audio.mp3",
                    "audio/mpeg",
                    "some data".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart(BASE_URL)
                            .file(wrongPartFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Required request part 'file' is missing."));
        }

        @Test
        @DisplayName("Debe retornar 413 Payload Too Large cuando el archivo supera el límite configurado")
        void talk_WhenFileSizeExceedsLimit_ShouldReturn413PayloadTooLarge() throws Exception {
            // Given
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file",
                    "large.wav",
                    "audio/wav",
                    new byte[1024]
            );

            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new MaxUploadSizeExceededException(1024 * 1024));

            // When & Then
            mockMvc.perform(multipart(BASE_URL)
                            .file(largeFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isPayloadTooLarge())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("The audio file exceeds the maximum allowed upload size."));
        }

        @Test
        @DisplayName("Debe retornar 502 Bad Gateway cuando ocurre un error de cliente REST o timeout con la API de IA")
        void talk_WhenExternalApiFails_ShouldReturn502BadGateway() throws Exception {
            // Given
            MockMultipartFile audioFile = new MockMultipartFile(
                    "file",
                    "voice.wav",
                    "audio/wav",
                    "audio data".getBytes()
            );

            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new ResourceAccessException("Connection timed out"));

            // When & Then
            mockMvc.perform(multipart(BASE_URL)
                            .file(audioFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadGateway())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("AI service communication error. Please try again later."));
        }

        @Test
        @DisplayName("Debe retornar 502 Bad Gateway cuando la API de IA devuelve un error HTTP 4xx/5xx (RestClientResponseException)")
        void talk_WhenExternalApiReturnsErrorStatus_ShouldReturn502BadGateway() throws Exception {
            // Given
            MockMultipartFile audioFile = new MockMultipartFile(
                    "file",
                    "voice.wav",
                    "audio/wav",
                    "audio data".getBytes()
            );

            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"));

            // When & Then
            mockMvc.perform(multipart(BASE_URL)
                            .file(audioFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadGateway())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("AI service communication error. Please try again later."));
        }

        @Test
        @DisplayName("Debe retornar 500 Internal Server Error ante cualquier excepción no controlada")
        void talk_WhenUnhandledExceptionOccurs_ShouldReturn500InternalServerError() throws Exception {
            // Given
            MockMultipartFile audioFile = new MockMultipartFile(
                    "file",
                    "voice.wav",
                    "audio/wav",
                    "audio data".getBytes()
            );

            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new RuntimeException("Database down or null pointer unexpected error"));

            // When & Then
            mockMvc.perform(multipart(BASE_URL)
                            .file(audioFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred on the server."));
        }
    }
}