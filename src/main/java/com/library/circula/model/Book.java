package com.library.circula.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books", indexes = @Index(name = "idx_book_isbn", columnList = "isbn"))
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32)
    private String isbn;
    @Column(nullable = false, length = 250)
    private String title;
    @Column(nullable = false, length = 160)
    private String author;

    protected Book() {
    }

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
