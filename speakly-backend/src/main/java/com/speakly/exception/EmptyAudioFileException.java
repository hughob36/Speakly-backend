package com.speakly.exception;

public class EmptyAudioFileException extends RuntimeException{
    public EmptyAudioFileException(String message) {
        super(message);
    }
}
