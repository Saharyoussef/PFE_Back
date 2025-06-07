package com.sahar.userservice.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter

// Jackson's annotation to ignore any unknown JSON properties that may come in the request payload.
// This prevents errors if the client sends extra fields not defined in this class.
@JsonIgnoreProperties(ignoreUnknown = true)

// It is meant to represent the data a client sends when trying to update or change a password.
public class PasswordRequest {
    @NotEmpty(message = "Password cannot be empty or null")
    private String currentPassword;
    @NotEmpty(message = "New password cannot be empty or null")
    private String newPassword;
    @NotEmpty(message = "Confirm password cannot be empty or null")
    private String confirmNewPassword;
}