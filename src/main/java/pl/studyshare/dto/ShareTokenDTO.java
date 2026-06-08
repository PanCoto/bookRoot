package pl.studyshare.dto;

public record ShareTokenDTO(
        String token,
        Long taskId,
        String taskTitle,
        String recipientName,
        String publicUrl
) {}