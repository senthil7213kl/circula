package com.library.circula.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class Requests {
    private Requests() {
    }

    public record RegisterBorrower(
            @Schema(example = "Ada Lovelace") @NotBlank @Size(max = 120) String name,
            @Schema(example = "ada@example.com") @NotBlank @Email @Size(max = 254) String email) {
    }

    public record RegisterBook(
            @Schema(example = "9780132350884") @NotBlank @Size(max = 32) String isbn,
            @Schema(example = "Clean Code") @NotBlank @Size(max = 250) String title,
            @Schema(example = "Robert C. Martin") @NotBlank @Size(max = 160) String author) {
    }
}
