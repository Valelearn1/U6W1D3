package com.example.demo.dto.openrouter;

/** Un messaggio nel formato che si aspetta OpenRouter: {"role": "...", "content": "..."} */
public record WireMessage(String role, String content) {

    public static WireMessage user(String content) {
        return new WireMessage("user", content);
    }

    public static WireMessage assistant(String content) {
        return new WireMessage("assistant", content);
    }
}
