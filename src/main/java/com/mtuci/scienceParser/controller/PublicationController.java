package com.mtuci.scienceParser.controller;

import com.mtuci.scienceParser.model.Publication;
import com.mtuci.scienceParser.service.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/parse")
@RequiredArgsConstructor
public class PublicationController {
    @Autowired
    private final PublicationService articleService;

    @GetMapping("/getPublicationInTopic")
    public ResponseEntity<List<Publication>> getPublicationsInTopic(@RequestParam("requestInTopic") String request, @RequestParam(value = "numberOfPagesInTopic", defaultValue = "20") Integer numberOfPages) throws InterruptedException {
        List<Publication> publication = articleService.parsePublicationInTopic(request, numberOfPages);
        return ResponseEntity.ok().body(publication);
    }

    @GetMapping("/getPublicationInSearch")
    public ResponseEntity<List<Publication>> getPublicationsInSearch(@RequestParam("requestInSearch") String request, @RequestParam(value = "numberOfPagesInSearch", defaultValue = "20") Integer numberOfPages) throws InterruptedException {
        //List<Publication> publication = articleService.parsePublicationInSearch(request, numberOfPages);
        return ResponseEntity.ok().body(articleService.parsePublicationInSearch(request, numberOfPages));
    }

    @GetMapping
    public String home(){
        return "ScienceParserGUI";
    }
}
