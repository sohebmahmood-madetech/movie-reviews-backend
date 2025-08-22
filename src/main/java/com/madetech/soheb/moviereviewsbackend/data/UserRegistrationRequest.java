package com.madetech.soheb.moviereviewsbackend.data;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 256)
    private String password;

    @NotNull
    private LocalDate dateOfBirth;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRegistrationRequest that = (UserRegistrationRequest) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(email, that.email) &&
                Objects.equals(dateOfBirth, that.dateOfBirth);
        // Note: Password deliberately excluded from equals/hashCode for security reasons
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, dateOfBirth);
        // Note: Password deliberately excluded from equals/hashCode for security reasons
    }
}