package com.library.circula.api;

import com.library.circula.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LibraryController {
    private final LibraryService service;

    public LibraryController(LibraryService service) {
        this.service = service;
    }

    @PostMapping("/borrowers")
    @Operation(summary = "Register a borrower", description = "Example: Ada Lovelace (ada@example.com). The server generates the borrower ID.")
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.BorrowerView registerBorrower(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Borrower details", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Borrower", externalValue = "/examples/borrower.json")))
            @Valid @RequestBody Requests.RegisterBorrower request) {
        return service.registerBorrower(request);
    }

    @PostMapping("/books")
    @Operation(summary = "Register a book copy", description = "Example: Clean Code by Robert C. Martin, ISBN 9780132350884. The server generates the copy ID.")
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.BookView registerBook(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Book copy details", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Book", externalValue = "/examples/book.json")))
            @Valid @RequestBody Requests.RegisterBook request) {
        return service.registerBook(request);
    }

    @GetMapping("/books")
    public List<Responses.BookView> listBooks() {
        return service.listBooks();
    }

    @PostMapping("/borrowers/{borrowerId}/books/{bookId}/borrow")
    @Operation(summary = "Borrow a book copy", description = "Creates a loan for the specified borrower and book copy. No request body is needed; use the IDs returned when registering them.")
    @ResponseStatus(HttpStatus.CREATED)
    public Responses.LoanView borrow(
            @Parameter(description = "Borrower ID returned by registration", example = "1") @PathVariable Long borrowerId,
            @Parameter(description = "Book copy ID returned by registration", example = "1") @PathVariable Long bookId) {
        return service.borrow(borrowerId, bookId);
    }

    @PostMapping("/borrowers/{borrowerId}/books/{bookId}/return")
    @Operation(summary = "Return a borrowed book copy", description = "Returns the active loan for this borrower and book copy.")
    public Responses.LoanView returnBook(
            @Parameter(description = "Borrower ID returned by registration", example = "1") @PathVariable Long borrowerId,
            @Parameter(description = "Book copy ID returned by registration", example = "1") @PathVariable Long bookId) {
        return service.returnBook(borrowerId, bookId);
    }
}
