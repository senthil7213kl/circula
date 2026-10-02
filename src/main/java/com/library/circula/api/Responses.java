package com.library.circula.api;

import com.library.circula.model.Book;
import com.library.circula.model.Borrower;
import com.library.circula.model.Loan;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public final class Responses {
    private Responses() {
    }

    public record BorrowerView(@Schema(example = "1") Long id,
                               @Schema(example = "Ada Lovelace") String name,
                               @Schema(example = "ada@example.com") String email) {
        public static BorrowerView from(Borrower b) {
            return new BorrowerView(b.getId(), b.getName(), b.getEmail());
        }
    }

    public record BookView(@Schema(example = "1") Long id,
                           @Schema(example = "9780132350884") String isbn,
                           @Schema(example = "Clean Code") String title,
                           @Schema(example = "Robert C. Martin") String author,
                           @Schema(example = "true") boolean available) {
        public static BookView from(Book b, boolean available) {
            return new BookView(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(), available);
        }
    }

    public record LoanView(@Schema(example = "1") Long id,
                           @Schema(example = "1") Long bookId,
                           @Schema(example = "1") Long borrowerId,
                           @Schema(example = "2026-10-02T10:00:00Z") Instant borrowedAt,
                           @Schema(example = "null", nullable = true) Instant returnedAt) {
        public static LoanView from(Loan l) {
            return new LoanView(l.getId(), l.getBook().getId(), l.getBorrower().getId(), l.getBorrowedAt(), l.getReturnedAt());
        }
    }
}
