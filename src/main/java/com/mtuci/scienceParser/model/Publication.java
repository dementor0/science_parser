package com.mtuci.scienceParser.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
@Entity
//@NoArgsConstructor
@Table(name = "publication")
public class Publication {
//    public Publication(){
//        id = UUID.randomUUID();
//    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String type;

    private String textAvailable;

    private Date dateCompletion;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "authors_id")
    private List<Author> authors;

    private String annotation;

    private String urlOnPublication;

    private String urlForDownload;
}
