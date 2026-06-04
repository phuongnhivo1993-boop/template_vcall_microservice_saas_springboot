package com.vcall.knowledgebase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArticleRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    private String category;

    private String tags;
}
