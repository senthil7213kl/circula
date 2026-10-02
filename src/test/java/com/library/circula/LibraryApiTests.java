package com.library.circula;

import com.library.circula.repository.BookRepository;
import com.library.circula.repository.BorrowerRepository;
import com.library.circula.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:circula-integration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
class LibraryApiTests {
    @Autowired
    MockMvc mvc;
    @Autowired
    LoanRepository loans;
    @Autowired
    BookRepository books;
    @Autowired
    BorrowerRepository borrowers;

    @BeforeEach
    void clearDatabase() {
        loans.deleteAll();
        books.deleteAll();
        borrowers.deleteAll();
    }

    @Test
    void registersBorrowerAndNormalizesEmail() throws Exception {
        mvc.perform(post("/api/borrowers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada Lovelace\",\"email\":\"ADA@EXAMPLE.COM\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void rejectsInvalidBorrowerAndDuplicateEmail() throws Exception {
        mvc.perform(post("/api/borrowers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.email").exists());

        registerBorrower();
        mvc.perform(post("/api/borrowers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada Again\",\"email\":\"ADA@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void registersAndListsCopiesAndRejectsMetadataDisagreement() throws Exception {
        registerBook();
        registerBook();

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].isbn").value("9780132350884"));

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"9780132350884\",\"title\":\"Different title\",\"author\":\"Robert C. Martin\"}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"\",\"title\":\" \",\"author\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.isbn").exists());
    }

    @Test
    void listsEmptyCatalogAndCreatesIndependentCopiesWithSameMetadata() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        registerBook();
        registerBook();

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[1].id").isNumber());
    }

    @Test
    void borrowsReturnsAndReportsAvailability() throws Exception {
        long borrowerId = registerBorrower();
        long bookId = registerBook();

        mvc.perform(post("/api/borrowers/{borrowerId}/books/{bookId}/borrow", borrowerId, bookId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.borrowerId").value(borrowerId))
                .andExpect(jsonPath("$.bookId").value(bookId))
                .andExpect(jsonPath("$.borrowedAt").exists())
                .andExpect(jsonPath("$.returnedAt").doesNotExist());

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(false));

        mvc.perform(post("/api/borrowers/{borrowerId}/books/{bookId}/borrow", borrowerId, bookId))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/borrowers/{borrowerId}/books/{bookId}/return", borrowerId, bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnedAt").exists());

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true));

        mvc.perform(post("/api/borrowers/{borrowerId}/books/{bookId}/return", borrowerId, bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundForUnknownBorrowerOrBook() throws Exception {
        mvc.perform(post("/api/borrowers/404/books/404/borrow"))
                .andExpect(status().isNotFound());

        long borrowerId = registerBorrower();
        mvc.perform(post("/api/borrowers/{borrowerId}/books/404/borrow", borrowerId))
                .andExpect(status().isNotFound());

        long bookId = registerBook();
        mvc.perform(post("/api/borrowers/404/books/{bookId}/borrow", bookId))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/borrowers/{borrowerId}/books/{bookId}/return", borrowerId, bookId))
                .andExpect(status().isNotFound());
    }

    private long registerBorrower() throws Exception {
        String response = mvc.perform(post("/api/borrowers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada Lovelace\",\"email\":\"ada@example.com\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idFrom(response);
    }

    private long registerBook() throws Exception {
        String response = mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"9780132350884\",\"title\":\"Clean Code\",\"author\":\"Robert C. Martin\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idFrom(response);
    }

    private long idFrom(String json) throws Exception {
        return com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(json).get("id").asLong();
    }
}
