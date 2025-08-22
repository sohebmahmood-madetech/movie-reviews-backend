package com.madetech.soheb.moviereviewsbackend.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @NotNull
    private UUID id;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String username;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    
    @Column(name = "password_hash", nullable = false, length = 256)
    @NotBlank
    @Size(max = 256)
    private String passwordHash;
    
    @Column(name = "date_of_birth", nullable = false)
    @NotNull
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Column(nullable = false)
    private boolean rejected = false;
    
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return rejected == user.rejected &&
                Objects.equals(id, user.id) &&
                Objects.equals(username, user.username) &&
                Objects.equals(email, user.email) &&
                Objects.equals(passwordHash, user.passwordHash) &&
                Objects.equals(dateOfBirth, user.dateOfBirth) &&
                Objects.equals(createdAt, user.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, passwordHash, dateOfBirth, rejected, createdAt);
    }
}