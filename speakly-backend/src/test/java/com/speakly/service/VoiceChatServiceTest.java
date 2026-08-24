package com.speakly.service;

import com.speakly.dto.TutorResponseDTO;
import com.speakly.exception.EmptyAudioFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoiceChatServiceTest {

    @Mock
    private OpenAiAudioTranscriptionModel transcriptionModel;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private MultipartFile audioFile;

    @Mock
    private Resource audioResource;

    private VoiceChatService voiceChatService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        voiceChatService = new VoiceChatService(transcriptionModel, chatClientBuilder);
    }

    @Test
    @DisplayName("Debe procesar el audio correctamente y devolver la transcripción junto a la respuesta del tutor")
    void processVoiceConversation_Success() {
        // Arrange
        String transcribedText = "Hello, how are you today?";
        String tutorReply = "Great job! I am doing well, thank you.";

        when(audioFile.isEmpty()).thenReturn(false);
        when(audioFile.getResource()).thenReturn(audioResource);

        // 1. Mock Transcription
        AudioTranscription transcription = new AudioTranscription(transcribedText);
        AudioTranscriptionResponse transcriptionResponse = new AudioTranscriptionResponse(transcription);
        when(transcriptionModel.call(any(AudioTranscriptionPrompt.class))).thenReturn(transcriptionResponse);

        // 2. Mock Fluent ChatClient
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(transcribedText)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(tutorReply);

        // Act
        TutorResponseDTO result = voiceChatService.processVoiceConversation(audioFile);

        // Assert
        assertNotNull(result);
        assertEquals(transcribedText, result.userSaid());
        assertEquals(tutorReply, result.tutorReply());

        verify(transcriptionModel, times(1)).call(any(AudioTranscriptionPrompt.class));
        verify(chatClient, times(1)).prompt();
        verify(requestSpec, times(1)).call();
    }

    @Test
    @DisplayName("Debe lanzar EmptyAudioFileException cuando el archivo MultipartFile es null")
    void processVoiceConversation_ThrowsException_WhenAudioFileIsNull() {
        EmptyAudioFileException exception = assertThrows(
                EmptyAudioFileException.class,
                () -> voiceChatService.processVoiceConversation(null)
        );

        assertEquals("The provided audio file is empty or missing.", exception.getMessage());
        verifyNoInteractions(transcriptionModel);
        verifyNoInteractions(chatClient);
    }

    @Test
    @DisplayName("Debe lanzar EmptyAudioFileException cuando el archivo de audio está vacío")
    void processVoiceConversation_ThrowsException_WhenAudioFileIsEmpty() {
        when(audioFile.isEmpty()).thenReturn(true);

        EmptyAudioFileException exception = assertThrows(
                EmptyAudioFileException.class,
                () -> voiceChatService.processVoiceConversation(audioFile)
        );

        assertEquals("The provided audio file is empty or missing.", exception.getMessage());
        verifyNoInteractions(transcriptionModel);
        verifyNoInteractions(chatClient);
    }
}