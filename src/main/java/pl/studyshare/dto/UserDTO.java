package pl.studyshare.dto;

import pl.studyshare.enums.Role;

public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String login,
        int age,
        Role role,
        boolean enabled
) {}