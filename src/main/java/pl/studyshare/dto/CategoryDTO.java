package pl.studyshare.dto;

public record CategoryDTO(
        Long id,
        String name,
        long taskCount
) {}