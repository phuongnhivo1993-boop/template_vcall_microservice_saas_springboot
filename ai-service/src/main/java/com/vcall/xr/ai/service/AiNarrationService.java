package com.vcall.xr.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiNarrationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.base-url}")
    private String baseUrl;

    @Value("${ai.tts.voice}")
    private String defaultVoice;

    public Map<String, Object> generateNarration(String text, String voice, String speed) {
        log.info("Generating narration for text of length: {} with voice: {}", text.length(), voice);

        Map<String, Object> response = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build()
                .post()
                .uri("/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", "tts-1",
                        "input", text,
                        "voice", voice != null ? voice : defaultVoice,
                        "response_format", "mp3",
                        "speed", speed != null ? Double.parseDouble(speed) : 1.0
                ))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        return Map.of(
                "status", "generated",
                "format", "mp3",
                "voice", voice != null ? voice : defaultVoice
        );
    }

    public Map<String, Object> generateNarrationScript(String sceneDescription, String tone, String language) {
        log.info("Generating narration script for scene with tone: {}", tone);

        String systemPrompt = "You are a professional XR experience narrator. " +
                "Write an engaging narration script for the given scene description. " +
                "Tone: " + (tone != null ? tone : "informative and engaging") + ". " +
                "Language: " + (language != null ? language : "English") + ". " +
                "Return a JSON object with keys: title, narration (string), segments (array of {text, timestamp, action}).";

        Map<String, Object> response = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build()
                .post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", "gpt-4",
                        "messages", java.util.List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", sceneDescription)
                        ),
                        "temperature", 0.7,
                        "max_tokens", 2048
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return Map.of("script", response);
    }
}
