package com.mtuci.scienceParser.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "author")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;      //порядковый номер

    private Long idArticle;

    private String name;

    private String url;
}
