package com.example.voicechanger.exception;

public class FreeSwitchConfigException extends RuntimeException {
    public FreeSwitchConfigException(String message) {
        super(message);
    }

    public FreeSwitchConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
