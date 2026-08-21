package com.speakly.controller;

import com.speakly.dto.TutorResponseDTO;
import com.speakly.service.VoiceChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VoiceChatController {

    private final VoiceChatService voiceChatService;

    @PostMapping(value = "/talk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TutorResponseDTO> talk(@RequestParam("file") MultipartFile audioFile) {

        TutorResponseDTO tutorReply = voiceChatService.processVoiceConversation(audioFile);
        return ResponseEntity.ok(tutorReply);
    }
}