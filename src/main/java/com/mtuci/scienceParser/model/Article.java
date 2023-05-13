package com.mtuci.scienceParser.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "publication")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;      //порядковый номер

    @Column(name = "title",unique = true,nullable = false)
    private String title; // заголовок статьи

    @Column(name = "type", nullable = false)
    private String type; // вид статьи (публикация, статья, пост)

    @Column(name = "text_available", nullable = false)
    private String textAvailable; // уровень доступности (полное содержание или нет)

    @Column(name = "date_completion", nullable = false)
    private Date dateCompletion; // дата публикации

    @Column(name = "authors", nullable = false)
    private String authors; // авторы

    @Column(name = "annotation", nullable = false)
    private String annotation; // аннотация

    @Column(name = "url_on_publication", nullable = false)
    private String urlOnPublication; //ссылка на статью

    @Column(name = "url_for_download", nullable = false)
    private String urlForDownload; // ссылка на скачивание
}
