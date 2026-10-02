package com.library.circula;

import com.library.circula.api.Requests;
import com.library.circula.model.Book;
import com.library.circula.model.Borrower;
import com.library.circula.repository.BookRepository;
import com.library.circula.repository.BorrowerRepository;
import com.library.circula.repository.LoanRepository;
import com.library.circula.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTests {
    @Mock
    BorrowerRepository borrowers;
    @Mock
    BookRepository books;
    @Mock
    LoanRepository loans;
    @InjectMocks
    LibraryService service;

    @Test
    void givenMixedCaseEmail_whenRegisteringBorrower_thenStoresTrimmedLowercaseEmail() {
        when(borrowers.saveAndFlush(any(Borrower.class))).thenAnswer(call -> call.getArgument(0));

        var result = service.registerBorrower(new Requests.RegisterBorrower(" Ada Lovelace ", " ADA@EXAMPLE.COM "));

        assertEquals("ada@example.com", result.email());
        assertEquals("Ada Lovelace", result.name());
        verify(borrowers).existsByEmailIgnoreCase("ada@example.com");
    }

    @Test
    void givenExistingEmail_whenRegisteringBorrower_thenReturnsConflict() {
        when(borrowers.existsByEmailIgnoreCase("ada@example.com")).thenReturn(true);

        var error = assertThrows(ResponseStatusException.class, () ->
                service.registerBorrower(new Requests.RegisterBorrower("Ada", "ada@example.com")));

        assertEquals(409, error.getStatusCode().value());
        verify(borrowers, never()).saveAndFlush(any());
    }

    @Test
    void givenDifferentBookMetadataForIsbn_whenRegisteringBook_thenReturnsConflict() {
        when(books.existsByIsbnWithDifferentMetadata("123", "New title", "Author")).thenReturn(true);

        var error = assertThrows(ResponseStatusException.class, () ->
                service.registerBook(new Requests.RegisterBook("123", "New title", "Author")));

        assertEquals(409, error.getStatusCode().value());
        verify(books, never()).save(any(Book.class));
    }

    @Test
    void givenUnknownBorrower_whenBorrowing_thenReturnsNotFound() {
        when(borrowers.findById(42L)).thenReturn(Optional.empty());

        var error = assertThrows(ResponseStatusException.class, () -> service.borrow(42L, 9L));

        assertEquals(404, error.getStatusCode().value());
        verify(books, never()).findByIdForUpdate(any());
    }
}
