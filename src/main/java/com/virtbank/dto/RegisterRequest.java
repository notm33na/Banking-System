package com.virtbank.dto;

import com.virtbank.validation.SafeText;
import com.virtbank.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @SafeText
    private String firstName;

    @NotBlank(message = "Last name is required")
    @SafeText
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @StrongPassword
    private String password;

    private String phone;

    @NotBlank(message = "User type is required (CUSTOMER, BUSINESS, or ADMIN)")
    private String userType;
}
