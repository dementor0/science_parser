package com.mtuci.scienceParser.repository;

import com.mtuci.scienceParser.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
