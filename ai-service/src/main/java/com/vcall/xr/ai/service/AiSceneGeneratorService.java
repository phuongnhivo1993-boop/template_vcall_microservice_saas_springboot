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
public class AiSceneGeneratorService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.base-url}")
    private String baseUrl;

    @Value("${ai.openai.model}")
    private String model;

    public Map<String, Object> generateScene(String prompt, String style, String language) {
        log.info("Generating scene from prompt: {} with style: {}", prompt, style);

        String systemPrompt = buildSceneSystemPrompt(style, language);
        String userPrompt = prompt;

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
                                Map.of("role", "user", "content", userPrompt)
                        ),
                        "temperature", 0.7,
                        "max_tokens", 4096
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseSceneResponse(response);
    }

    public Map<String, Object> refineScene(String sceneId, String refinementPrompt) {
        log.info("Refining scene: {} with prompt: {}", sceneId, refinementPrompt);

        String systemPrompt = "You are an XR scene editor. Refine the given scene based on the user's instructions. Return the updated scene configuration as JSON.";

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
                                Map.of("role", "user", "content", "Scene ID: " + sceneId + "\n\nRefinement: " + refinementPrompt)
                        ),
                        "temperature", 0.5,
                        "max_tokens", 4096
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseSceneResponse(response);
    }

    public Map<String, Object> generateSceneVariations(String sceneId, int count) {
        log.info("Generating {} variations for scene: {}", count, sceneId);

        String systemPrompt = "You are an XR scene designer. Generate variations of the given scene with different visual styles, lighting, and atmosphere. Return each variation as a JSON object.";

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
                                Map.of("role", "user", "content", "Scene ID: " + sceneId + "\n\nGenerate " + count + " variations.")
                        ),
                        "temperature", 0.9,
                        "max_tokens", 4096
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseSceneResponse(response);
    }

    private String buildSceneSystemPrompt(String style, String language) {
        return "You are an expert XR scene designer for a WebXR platform. " +
                "Generate a complete XR scene configuration based on the user's description. " +
                "Style preference: " + (style != null ? style : "modern, clean") + ". " +
                "Language: " + (language != null ? language : "English") + ". " +
                "Return the scene as a JSON object with keys: name, description, objects (array of 3D objects), " +
                "lighting (object), environment (object), interactions (array). " +
                "Each object in objects array should have: type, position [x,y,z], rotation [x,y,z], scale [x,y,z], material, color.";
    }

    private Map<String, Object> parseSceneResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("choices")) {
            throw new RuntimeException("Invalid response from AI service");
        }
        java.util.List<Map<String, Object>> choices = (java.util.List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in AI response");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");
        return Map.of("sceneConfig", content, "rawResponse", response);
    }
}
