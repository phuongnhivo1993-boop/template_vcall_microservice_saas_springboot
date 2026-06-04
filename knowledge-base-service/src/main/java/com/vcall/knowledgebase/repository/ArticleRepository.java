package com.vcall.knowledgebase.repository;

import com.vcall.knowledgebase.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    Page<Article> findByStatusOrderByUpdatedAtDesc(String status, Pageable pageable);

    long countByStatus(String status);
}
