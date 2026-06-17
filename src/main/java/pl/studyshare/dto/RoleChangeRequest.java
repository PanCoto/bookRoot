package pl.studyshare.dto;

import pl.studyshare.enums.Role;

import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(
        @NotNull(message = "Rola jest wymagana")
        Role role
) {}
