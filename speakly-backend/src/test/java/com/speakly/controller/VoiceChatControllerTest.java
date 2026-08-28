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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VoiceChatController.class)
@Import(GlobalExceptionHandler.class)
class VoiceChatControllerTest {

    private static final String ENDPOINT = "/api/voice/talk";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VoiceChatService voiceChatService;

    @Nested
    @DisplayName("Positive Scenarios")
    class PositiveScenarios {

        @Test
        @DisplayName("Should return 200 OK with transcription and AI reply when a valid audio file is provided")
        void talk_WithValidAudioFile_ShouldReturn200AndTutorResponse() throws Exception {
            MockMultipartFile validFile = new MockMultipartFile(
                    "file",
                    "audio.wav",
                    "audio/wav",
                    "dummy audio content bytes".getBytes()
            );

            TutorResponseDTO mockResponse = new TutorResponseDTO(
                    "I goes to store yesterday",
                    "You should say 'I went to the store yesterday'. I love shopping on weekends! What did you buy?"
            );

            given(voiceChatService.processVoiceConversation(any())).willReturn(mockResponse);

            mockMvc.perform(multipart(ENDPOINT).file(validFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.userSaid").value("I goes to store yesterday"))
                    .andExpect(jsonPath("$.tutorReply").value(
                            "You should say 'I went to the store yesterday'. I love shopping on weekends! What did you buy?"
                    ));
        }
    }

    @Nested
    @DisplayName("Negative Scenarios")
    class NegativeScenarios {

        @Test
        @DisplayName("Should return 400 Bad Request when service throws EmptyAudioFileException")
        void talk_WithEmptyAudioFile_ShouldReturn400BadRequest() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.wav",
                    "audio/wav",
                    new byte[0]
            );

            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new EmptyAudioFileException("The provided audio file is empty or missing."));

            mockMvc.perform(multipart(ENDPOINT).file(emptyFile))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.message").value("The provided audio file is empty or missing."));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when 'file' multipart parameter is missing")
        void talk_WithMissingFileParam_ShouldReturn400BadRequest() throws Exception {
            MockMultipartFile wrongParamFile = new MockMultipartFile(
                    "wrong_param",
                    "sample.wav",
                    "audio/wav",
                    "data".getBytes()
            );

            mockMvc.perform(multipart(ENDPOINT).file(wrongParamFile))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.message").value("Required request part 'file' is missing."));
        }

        @Test
        @DisplayName("Should return 413 Payload Too Large when file size exceeds server limit")
        void talk_WhenFileSizeExceedsLimit_ShouldReturn413PayloadTooLarge() throws Exception {
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file",
                    "large.wav",
                    "audio/wav",
                    "large payload".getBytes()
            );

            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new MaxUploadSizeExceededException(10485760));

            mockMvc.perform(multipart(ENDPOINT).file(largeFile))
                    .andExpect(status().isPayloadTooLarge())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.message").value("The audio file exceeds the maximum allowed upload size."));
        }

        @Test
        @DisplayName("Should return 502 Bad Gateway when external AI API fails with RestClientResponseException")
        void talk_WhenAiApiReturnsClientError_ShouldReturn502BadGateway() throws Exception {
            MockMultipartFile validFile = new MockMultipartFile(
                    "file",
                    "test.wav",
                    "audio/wav",
                    "valid audio content".getBytes()
            );

            RestClientResponseException apiException = new RestClientResponseException(
                    "External AI service error",
                    500,
                    "Internal Server Error",
                    HttpHeaders.EMPTY,
                    new byte[0],
                    null
            );

            given(voiceChatService.processVoiceConversation(any())).willThrow(apiException);

            mockMvc.perform(multipart(ENDPOINT).file(validFile))
                    .andExpect(status().isBadGateway())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.message").value("AI service communication error. Please try again later."));
        }

        @Test
        @DisplayName("Should return 502 Bad Gateway when external AI API times out via ResourceAccessException")
        void talk_WhenAiApiTimesOut_ShouldReturn502BadGateway() throws Exception {
            MockMultipartFile validFile = new MockMultipartFile(
                    "file",
                    "test.wav",
                    "audio/wav",
                    "valid audio content".getBytes()
            );

            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new ResourceAccessException("Connection timed out"));

            mockMvc.perform(multipart(ENDPOINT).file(validFile))
                    .andExpect(status().isBadGateway())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.message").value("AI service communication error. Please try again later."));
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error when an unhandled exception occurs")
        void talk_WhenUnhandledExceptionOccurs_ShouldReturn500InternalServerError() throws Exception {
            MockMultipartFile validFile = new MockMultipartFile(
                    "file",
                    "test.wav",
                    "audio/wav",
                    "valid audio content".getBytes()
            );

            given(voiceChatService.processVoiceConversation(any()))
                    .willThrow(new RuntimeException("Unexpected database or internal failure"));

            mockMvc.perform(multipart(ENDPOINT).file(validFile))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred on the server."));
        }
    }
}