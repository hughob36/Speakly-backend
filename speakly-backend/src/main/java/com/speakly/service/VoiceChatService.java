package com.speakly.service;

import com.speakly.dto.TutorResponseDTO;
import com.speakly.exception.EmptyAudioFileException;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VoiceChatService {


    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;

     public VoiceChatService(OpenAiAudioTranscriptionModel transcriptionModel,
                               ChatClient.Builder chatClientBuilder) {
        this.transcriptionModel = transcriptionModel;
        this.chatClient = chatClientBuilder.build();
    }

    public TutorResponseDTO processVoiceConversation(MultipartFile audioFile) {

        if (audioFile == null || audioFile.isEmpty()) {
            throw new EmptyAudioFileException("The provided audio file is empty or missing.");
        }

        // 1. Transcripción con Groq Whisper
        var transcriptionResponse = transcriptionModel.call(
                new AudioTranscriptionPrompt(audioFile.getResource())
        );
        String userText = transcriptionResponse != null && transcriptionResponse.getResult() != null
                ? transcriptionResponse.getResult().getOutput()
                : null;

        // 2. Si la transcripción viene vacía o nula, responder con mensaje por defecto
        if (userText == null || userText.isBlank()) {
            String fallbackText = (userText == null) ? "" : userText;
            return new TutorResponseDTO(fallbackText, "Could you repeat that? I couldn't hear you.");
        }
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


        return new TutorResponseDTO(userText, tutorReply);
    }

}
