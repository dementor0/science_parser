package com.mtuci.scienceParser.controller;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import com.mtuci.scienceParser.model.Article;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.mtuci.scienceParser.service.ArticleService;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/parse")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
//    @GetMapping("/getPublication")
//    public ResponseEntity<Article> getPublications(@RequestParam("request") String request, @RequestParam(value = "numberOfPages",defaultValue = "70") Integer numberOfPages) {
//        return ResponseEntity.ok().body(articleService.parseArticle(request, numberOfPages));
//    }
    @GetMapping("/getPublication")
    public ResponseEntity<List<String>> getPublications(@RequestBody Map<String, Object> requestMap) throws InterruptedException {
        String request = requestMap.get("request").toString();
        int numberOfPages = (int) requestMap.get("numberOfPages");
        return ResponseEntity.ok().body(articleService.findSearchUrlForPublication(request, numberOfPages));
    }

}
