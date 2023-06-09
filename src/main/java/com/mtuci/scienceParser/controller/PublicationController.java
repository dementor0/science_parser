package com.mtuci.scienceParser.controller;

import com.mtuci.scienceParser.model.AuthorInfo;
import com.mtuci.scienceParser.model.Publication;
import com.mtuci.scienceParser.service.PublicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PublicationController {
    @Autowired
    private final PublicationService publicationService;

    @GetMapping("/getPublicationInTopic")
    public ResponseEntity<List<Publication>> getPublicationsInTopic(@RequestParam("requestInTopic") String request, @RequestParam(value = "numberOfPagesInTopic", defaultValue = "20") Integer numberOfPages) throws InterruptedException {
        return ResponseEntity.ok().body(publicationService.parsePublicationInTopic(request, numberOfPages));
    }

    @GetMapping("/getPublicationInSearch")
    public ResponseEntity<List<Publication>> getPublicationsInSearch(@RequestParam("requestInSearch") String request, @RequestParam(value = "numberOfPagesInSearch", defaultValue = "20") Integer numberOfPages) throws InterruptedException {
        return ResponseEntity.ok().body(publicationService.parsePublicationInSearch(request, numberOfPages));
    }

    @GetMapping("/getAuthorInfo")
    public ResponseEntity<AuthorInfo> getAuthorInfo(@RequestParam("requestNameAuthor") String request) {
        return ResponseEntity.ok().body(publicationService.parseAuthorInfo(request));
    }

    @GetMapping
    public String home(){
        return "ScienceParserGUI";
    }
}
