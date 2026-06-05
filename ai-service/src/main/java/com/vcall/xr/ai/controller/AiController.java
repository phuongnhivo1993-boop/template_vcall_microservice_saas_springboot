package com.vcall.xr.ai.controller;

import com.vcall.common.dto.ApiResponse;
import com.vcall.xr.ai.service.AiNarrationService;
import com.vcall.xr.ai.service.AiSceneGeneratorService;
import com.vcall.xr.ai.service.AiTourGuideService;
import com.vcall.xr.ai.service.AiTranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiSceneGeneratorService sceneGeneratorService;
    private final AiNarrationService narrationService;
    private final AiTranslationService translationService;
    private final AiTourGuideService tourGuideService;

    @PostMapping("/scenes/generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateScene(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = sceneGeneratorService.generateScene(
                request.get("prompt"),
                request.get("style"),
                request.get("language")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Scene generated", result));
    }

    @PostMapping("/scenes/refine")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refineScene(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = sceneGeneratorService.refineScene(
                request.get("sceneId"),
                request.get("refinementPrompt")
        );
        return ResponseEntity.ok(ApiResponse.success("Scene refined", result));
    }

    @PostMapping("/scenes/variations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateSceneVariations(
            @RequestBody Map<String, Object> request) {
        Map<String, Object> result = sceneGeneratorService.generateSceneVariations(
                (String) request.get("sceneId"),
                (Integer) request.getOrDefault("count", 3)
        );
        return ResponseEntity.ok(ApiResponse.success("Variations generated", result));
    }

    @PostMapping("/narration/generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateNarration(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = narrationService.generateNarration(
                request.get("text"),
                request.get("voice"),
                request.get("speed")
        );
        return ResponseEntity.ok(ApiResponse.success("Narration generated", result));
    }

    @PostMapping("/narration/script")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateNarrationScript(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = narrationService.generateNarrationScript(
                request.get("sceneDescription"),
                request.get("tone"),
                request.get("language")
        );
        return ResponseEntity.ok(ApiResponse.success("Script generated", result));
    }

    @PostMapping("/translate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> translateText(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = translationService.translateText(
                request.get("text"),
                request.get("targetLanguage"),
                request.get("context")
        );
        return ResponseEntity.ok(ApiResponse.success("Translation completed", result));
    }

    @PostMapping("/translate/narration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> translateNarration(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = translationService.translateNarration(
                request.get("narrationId"),
                request.get("targetLanguage")
        );
        return ResponseEntity.ok(ApiResponse.success("Narration translated", result));
    }

    @PostMapping("/translate/detect")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detectLanguage(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = translationService.detectLanguage(request.get("text"));
        return ResponseEntity.ok(ApiResponse.success("Language detected", result));
    }

    @PostMapping("/tour-guide/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTourGuide(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = tourGuideService.createTourGuide(
                request.get("sceneId"),
                request.get("persona"),
                request.get("language")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tour guide created", result));
    }

    @PostMapping("/tour-guide/respond")
    public ResponseEntity<ApiResponse<Map<String, Object>>> guideRespond(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = tourGuideService.guideResponse(
                request.get("guideId"),
                request.get("userQuery"),
                request.get("sceneContext")
        );
        return ResponseEntity.ok(ApiResponse.success("Guide response generated", result));
    }

    @PostMapping("/tour-guide/route")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateTourRoute(
            @RequestBody Map<String, String> request) {
        Map<String, Object> result = tourGuideService.generateTourRoute(
                request.get("sceneId"),
                request.get("interests"),
                request.get("duration")
        );
        return ResponseEntity.ok(ApiResponse.success("Tour route generated", result));
    }
}
