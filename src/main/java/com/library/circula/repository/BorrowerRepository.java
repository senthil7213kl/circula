package com.library.circula.repository;

import com.library.circula.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
