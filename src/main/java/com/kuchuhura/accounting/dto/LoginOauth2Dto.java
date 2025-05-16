package com.kuchuhura.accounting.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginOauth2Dto(
        @Size(min = 2, max = 100, message = "Full Name should have at least 2 symbols")
        String fullName,
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is mandatory")
        String email
) {
}
