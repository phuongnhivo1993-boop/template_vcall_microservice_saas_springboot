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
public class AiTourGuideService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.base-url}")
    private String baseUrl;

    @Value("${ai.openai.model}")
    private String model;

    public Map<String, Object> createTourGuide(String sceneId, String persona, String language) {
        log.info("Creating tour guide for scene: {} with persona: {}", sceneId, persona);

        String systemPrompt = buildTourGuideSystemPrompt(persona, language);

        Map<String, Object> response = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build()
                .post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", model,
                        "messages", java.util.List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", "Create a tour guide persona for scene: " + sceneId)
                        ),
                        "temperature", 0.8,
                        "max_tokens", 2048
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseGuideResponse(response);
    }

    public Map<String, Object> guideResponse(String guideId, String userQuery, String sceneContext) {
        log.info("Generating guide response for query: {} in scene: {}", userQuery, sceneContext);

        String systemPrompt = "You are an AI tour guide in an XR experience. " +
                "Respond to the visitor's question in a helpful, engaging manner. " +
                "Keep responses concise but informative. " +
                "Scene context: " + (sceneContext != null ? sceneContext : "general XR environment");

        Map<String, Object> response = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build()
                .post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", model,
                        "messages", java.util.List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userQuery)
                        ),
                        "temperature", 0.7,
                        "max_tokens", 1024
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseGuideResponse(response);
    }

    public Map<String, Object> generateTourRoute(String sceneId, String interests, String duration) {
        log.info("Generating tour route for scene: {} with interests: {}", sceneId, interests);

        String systemPrompt = "You are an XR tour route planner. " +
                "Generate an optimized tour route through the scene based on the visitor's interests and available time. " +
                "Return a JSON object with keys: route (array of {pointId, name, description, duration, position}), " +
                "totalDuration, highlights, tips.";

        Map<String, Object> response = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build()
                .post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", model,
                        "messages", java.util.List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content",
                                        "Scene: " + sceneId + "\nInterests: " + interests + "\nDuration: " + duration)
                        ),
                        "temperature", 0.7,
                        "max_tokens", 4096
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseGuideResponse(response);
    }

    private String buildTourGuideSystemPrompt(String persona, String language) {
        return "You are an AI tour guide NPC in an XR experience. " +
                "Persona: " + (persona != null ? persona : "friendly, knowledgeable guide") + ". " +
                "Language: " + (language != null ? language : "English") + ". " +
                "You can describe objects, explain history, answer questions, and guide visitors through the XR environment. " +
                "Return responses as JSON with keys: response (text), action (optional action to perform), " +
                "emotion (guide's emotion), gazeTarget (where to look).";
    }

    private Map<String, Object> parseGuideResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("choices")) {
            throw new RuntimeException("Invalid response from tour guide service");
        }
        java.util.List<Map<String, Object>> choices = (java.util.List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in tour guide response");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        return Map.of("guideResponse", content, "rawResponse", response);
    }
}
