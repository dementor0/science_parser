package com.mtuci.scienceParser.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
@Entity
@Table(name = "publication")
public class Publication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;      //порядковый номер

    private String title; // заголовок статьи

    private String type; // вид статьи (публикация, статья, пост)

    private String textAvailable; // уровень доступности (полное содержание или нет)

    private Date dateCompletion; // дата публикации

//    @Column(name = "authors_id")
//    private Long authorsId;

    @Transient
    private List<String> authorNames; // список имен авторов

    // Геттер и сеттер для authorNames
    public List<String> getAuthorNames() {
        return authorNames;
    }

    public void setAuthorNames(List<String> authorNames) {
        this.authorNames = authorNames;
    }

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn
    private List<Author> authors; // авторы

    private String annotation; // аннотация

    private String urlOnPublication; //ссылка на статью

    private String urlForDownload; // ссылка на скачивание
}
