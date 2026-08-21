package com.speakly.controller;

import com.speakly.dto.TutorResponseDTO;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*")
public class VoiceChatController {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;

    public VoiceChatController(OpenAiAudioTranscriptionModel transcriptionModel,
                               ChatClient.Builder chatClientBuilder) {
        this.transcriptionModel = transcriptionModel;
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping(value = "/talk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TutorResponseDTO> talk(@RequestParam("file") MultipartFile audioFile) {
        if (audioFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 1. Transcripción con Groq Whisper
        var transcriptionResponse = transcriptionModel.call(
                new AudioTranscriptionPrompt(audioFile.getResource())
        );
        String userText = transcriptionResponse.getResult().getOutput();

        // 2. Llamada al LLM pasando el builder de opciones compatible con Spring AI
        String tutorReply = chatClient.prompt()
                .options(OpenAiChatOptions.builder()
                        .model("openai/gpt-oss-20b")
                        .temperature(0.3)
                        .maxTokens(500))
                .system("""
                    You are a friendly and encouraging English conversation tutor.
                    1. If the user makes any grammar, phrasing, or vocabulary mistake, gently correct it first in 1 concise sentence.
                    2. Continue the conversation naturally in 2 short sentences.
                    3. Always finish by asking an open-ended question to keep talking.
                    """)
                .user(userText)
                .call()
                .content();

        // 3. Retorno del DTO
        return ResponseEntity.ok(new TutorResponseDTO(userText, tutorReply));
    }
}