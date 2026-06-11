package pl.studyshare.dto;

import pl.studyshare.enums.Role;

import java.time.LocalDateTime;


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

    public boolean isActive() { return active; }
}