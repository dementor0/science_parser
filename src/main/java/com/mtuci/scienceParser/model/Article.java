package com.mtuci.scienceParser.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
@Entity
@Table(name = "publication")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;      //порядковый номер

    private String title; // заголовок статьи

    private String type; // вид статьи (публикация, статья, пост)

    private String textAvailable; // уровень доступности (полное содержание или нет)

    private Date dateCompletion; // дата публикации

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "idArticle", nullable = false)
    private List<Author> authors; // авторы

    private String annotation; // аннотация

    private String urlOnPublication; //ссылка на статью

    private String urlForDownload; // ссылка на скачивание
}
