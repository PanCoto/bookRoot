package pl.studyshare.dto;

import pl.studyshare.enums.Role;

import java.time.LocalDateTime;

/**
 * DTO representing a user in admin panel and profile views.
 * All fields are from User entity.
 */
public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String login,
        int age,
        Role role,
        boolean active,          // maps from User.enabled
        String email,
        LocalDateTime createdAt
) {
    /** Convenience alias: active reflects the user's enabled status. */
    public boolean isActive() { return active; }
}