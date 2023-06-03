package com.mtuci.scienceParser.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "author_info")
public class AuthorInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String url;

    private Float researchInterestScore;

    private Long citations;

    private Long hIndex;

    private Long amountPublication;

    @OneToMany
    @JoinColumn(name = "publication_id")
    private List<Publication> publications;
}

