package pl.studyshare.dto;

public record UserProfileDTO(
        String firstName,
        String lastName,
        String login,
        int age
) {}
