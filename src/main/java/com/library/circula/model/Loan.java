package com.library.circula.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "loans", indexes = @Index(name = "idx_loan_book_returned", columnList = "book_id, returned_at"))
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;
    @Column(nullable = false)
    private Instant borrowedAt;
    private Instant returnedAt;

    protected Loan() {
    }

    public Loan(Book book, Borrower borrower) {
        this.book = book;
        this.borrower = borrower;
        this.borrowedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public Instant getBorrowedAt() {
        return borrowedAt;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public void returnBook() {
        this.returnedAt = Instant.now();
    }
}
