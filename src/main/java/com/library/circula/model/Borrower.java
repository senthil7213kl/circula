package com.library.circula.model;

import jakarta.persistence.*;

@Entity
@Table(name = "borrowers", uniqueConstraints = @UniqueConstraint(name = "uk_borrower_email", columnNames = "email"))
public class Borrower {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 254)
    private String email;

    protected Borrower() {
    }

    public Borrower(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
