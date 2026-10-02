package com.library.circula.service;

import com.library.circula.api.Requests;
import com.library.circula.api.Responses;
import com.library.circula.model.Book;
import com.library.circula.model.Borrower;
import com.library.circula.model.Loan;
import com.library.circula.repository.BookRepository;
import com.library.circula.repository.BorrowerRepository;
import com.library.circula.repository.LoanRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LibraryService {
    private final BorrowerRepository borrowers;
    private final BookRepository books;
    private final LoanRepository loans;

    public LibraryService(BorrowerRepository borrowers, BookRepository books, LoanRepository loans) {
        this.borrowers = borrowers;
        this.books = books;
        this.loans = loans;
    }

    @Transactional
    public Responses.BorrowerView registerBorrower(Requests.RegisterBorrower r) {
        String email = r.email().trim().toLowerCase();
        if (borrowers.existsByEmailIgnoreCase(email)) throw conflict("A borrower with this email already exists");
        try {
            return Responses.BorrowerView.from(borrowers.saveAndFlush(new Borrower(r.name().trim(), email)));
        } catch (DataIntegrityViolationException ex) {
            throw conflict("A borrower with this email already exists");
        }
    }

    @Transactional
    public Responses.BookView registerBook(Requests.RegisterBook r) {
        String isbn = r.isbn().trim();
        String title = r.title().trim();
        String author = r.author().trim();
        if (books.existsByIsbnWithDifferentMetadata(isbn, title, author))
            throw conflict("An ISBN must always refer to the same title and author");
        return Responses.BookView.from(books.save(new Book(isbn, title, author)), true);
    }

    @Transactional(readOnly = true)
    public List<Responses.BookView> listBooks() {
        return books.findAll().stream().map(b -> Responses.BookView.from(b, !loans.existsByBookIdAndReturnedAtIsNull(b.getId()))).toList();
    }

    @Transactional
    public Responses.LoanView borrow(Long borrowerId, Long bookId) {
        Borrower borrower = borrowers.findById(borrowerId).orElseThrow(() -> notFound("Borrower", borrowerId));
        Book book = books.findByIdForUpdate(bookId).orElseThrow(() -> notFound("Book", bookId));
        if (loans.existsByBookIdAndReturnedAtIsNull(bookId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This book copy is already borrowed");
        return Responses.LoanView.from(loans.save(new Loan(book, borrower)));
    }

    @Transactional
    public Responses.LoanView returnBook(Long borrowerId, Long bookId) {
        loans.findByBookIdAndBorrowerIdAndReturnedAtIsNull(bookId, borrowerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active loan found for this borrower and book"));
        Book book = books.findByIdForUpdate(bookId).orElseThrow(() -> notFound("Book", bookId));
        Loan loan = loans.findByBookIdAndBorrowerIdAndReturnedAtIsNull(book.getId(), borrowerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active loan found for this borrower and book"));
        loan.returnBook();
        return Responses.LoanView.from(loan);
    }

    private static ResponseStatusException notFound(String type, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " " + id + " not found");
    }

    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
