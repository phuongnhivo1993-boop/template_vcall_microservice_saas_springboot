package com.vcall.knowledgebase.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.knowledgebase.dto.ArticleRequest;
import com.vcall.knowledgebase.dto.ArticleResponse;
import com.vcall.knowledgebase.entity.Article;
import com.vcall.knowledgebase.repository.ArticleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Page<ArticleResponse> getAllArticles(String category, String search, Pageable pageable) {
        Specification<Article> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("content")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return articleRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ArticleResponse getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        return toResponse(article);
    }

    @Transactional
    public ArticleResponse createArticle(ArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        article.setStatus("DRAFT");
        article.setViewCount(0);
        article = articleRepository.save(article);
        return toResponse(article);
    }

    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        article = articleRepository.save(article);
        return toResponse(article);
    }

    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        article.setIsDeleted(true);
        articleRepository.save(article);
    }

    @Transactional
    public ArticleResponse duplicateArticle(Long id) {
        Article original = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        Article copy = new Article();
        copy.setTitle(original.getTitle() + " (Copy)");
        copy.setContent(original.getContent());
        copy.setCategory(original.getCategory());
        copy.setTags(original.getTags());
        copy.setStatus("DRAFT");
        copy.setViewCount(0);
        copy = articleRepository.save(copy);
        return toResponse(copy);
    }

    @Transactional
    public ArticleResponse incrementViewCount(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        article.setViewCount(article.getViewCount() + 1);
        article = articleRepository.save(article);
        return toResponse(article);
    }

    private ArticleResponse toResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .category(article.getCategory())
                .tags(article.getTags())
                .status(article.getStatus())
                .viewCount(article.getViewCount())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
