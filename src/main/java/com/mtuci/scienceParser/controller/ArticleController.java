package com.mtuci.scienceParser.controller;

import com.mtuci.scienceParser.model.Article;
import com.mtuci.scienceParser.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/parse")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
//    @GetMapping("/getPublicationInSearch")
//    public ResponseEntity<Article> getPublicationsInSearch() {
//        return ResponseEntity.ok().body(articleService.parsePublicationInSearch());
//    }
    @GetMapping("/getPublicationInTopic")
    public ResponseEntity<List<Article>> getPublicationsInTopic(@RequestParam("request") String request, @RequestParam(value = "numberOfPages", defaultValue = "20") Integer numberOfPages) {
        List<Article> publication = articleService.parsePublication(request, numberOfPages);
        return ResponseEntity.ok().body(publication);
    }
}
