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
public class AiTranslationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.base-url}")
    private String baseUrl;

    @Value("${ai.translation.default-target-language}")
    private String defaultTargetLanguage;

    public Map<String, Object> translateText(String text, String targetLanguage, String context) {
        log.info("Translating text to {} with context: {}", targetLanguage, context);

        String systemPrompt = "You are a professional translator specializing in XR/VR/AR content localization. " +
                "Translate the given text accurately while maintaining technical terminology. " +
                "Context: " + (context != null ? context : "XR application") + ". " +
                "Return a JSON object with keys: translatedText, detectedLanguage, confidence.";

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
                                Map.of("role", "user", "content", "Translate to " +
                                        (targetLanguage != null ? targetLanguage : defaultTargetLanguage) + ":\n\n" + text)
                        ),
                        "temperature", 0.3,
                        "max_tokens", 4096
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseTranslationResponse(response, targetLanguage);
    }

    public Map<String, Object> translateNarration(String narrationId, String targetLanguage) {
        log.info("Translating narration {} to {}", narrationId, targetLanguage);

        String systemPrompt = "You are a professional XR narration translator. " +
                "Translate the narration script while preserving timing markers and emotional tone. " +
                "Return a JSON object with keys: narration (translated text), segments (array with timestamps preserved).";

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
                                Map.of("role", "user", "content", "Translate narration " + narrationId + " to " + targetLanguage)
                        ),
                        "temperature", 0.3,
                        "max_tokens", 4096
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return Map.of("translation", response, "targetLanguage", targetLanguage);
    }

    public Map<String, Object> detectLanguage(String text) {
        log.info("Detecting language for text of length: {}", text.length());

        String systemPrompt = "Detect the language of the given text. Return a JSON object with keys: language, languageName, confidence.";

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
                                Map.of("role", "user", "content", text)
                        ),
                        "temperature", 0.1,
                        "max_tokens", 100
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseTranslationResponse(response, null);
    }

    private Map<String, Object> parseTranslationResponse(Map<String, Object> response, String targetLanguage) {
        if (response == null || !response.containsKey("choices")) {
            throw new RuntimeException("Invalid response from translation service");
        }
        java.util.List<Map<String, Object>> choices = (java.util.List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in translation response");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        return Map.of("result", content, "targetLanguage", targetLanguage != null ? targetLanguage : "auto-detected");
    }
}
