package com.library.circula.repository;

import com.library.circula.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    boolean existsByBookIdAndReturnedAtIsNull(Long bookId);

    Optional<Loan> findByBookIdAndBorrowerIdAndReturnedAtIsNull(Long bookId, Long borrowerId);
}
