package pl.studyshare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.Share;
import pl.studyshare.domain.Task;
import pl.studyshare.domain.User;
import pl.studyshare.dto.ShareCreateRequest;
import pl.studyshare.dto.ShareTokenDTO;
import pl.studyshare.dto.TaskDTO;
import pl.studyshare.enums.Role;
import pl.studyshare.enums.ShareType;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.mapper.TaskMapper;
import pl.studyshare.repository.ShareRepository;
import pl.studyshare.repository.TaskRepository;
import pl.studyshare.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shareRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public ShareTokenDTO createShareToken(ShareCreateRequest request, String ownerUsername) {
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new IllegalArgumentException("Zadanie nie istnieje: " + request.taskId()));

        User owner = userRepository.findByLogin(ownerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + ownerUsername));

        boolean isAdmin = owner.getRole() == Role.ADMIN;
        boolean isAuthor = task.getAuthor() != null && task.getAuthor().getLogin().equals(ownerUsername);

        if (!isAdmin && !isAuthor) {
            throw new SecurityException("Nie masz uprawnień do udostępniania tego zadania");
        }

        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new IllegalStateException("Udostępniać można wyłącznie szkice zadań (status DRAFT)");
        }

        User recipient = null;
        if (request.recipientUserId() != null) {
            recipient = userRepository.findById(request.recipientUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Odbiorca o podanym ID nie istnieje"));
        }

        ShareType shareType = (recipient != null) ? ShareType.SPECIFIC_USER : ShareType.PUBLIC_LINK;

        String token = UUID.randomUUID().toString();

        Share share = Share.builder()
                .token(token)
                .shareType(shareType)
                .task(task)
                .owner(owner)
                .recipient(recipient)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        Share saved = shareRepository.save(share);

        String publicUrl = "/share/" + saved.getToken();

        return new ShareTokenDTO(
                saved.getToken(),
                task.getId(),
                task.getTitle(),
                recipient != null ? recipient.getLogin() : null,
                publicUrl
        );
    }

    @Transactional(readOnly = true)
    public TaskDTO findSharedTaskByToken(String token, String currentUsername) {
        Share share = shareRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowy token udostępniania"));

        if (share.isExpired()) {
            throw new IllegalStateException("Link udostępniania wygasł");
        }

        if (share.getRecipient() != null) {
            if (currentUsername == null) {
                throw new SecurityException("To zadanie zostało udostępnione konkretnemu użytkownikowi. Zaloguj się, aby je zobaczyć.");
            }
            if (!share.getRecipient().getLogin().equals(currentUsername)) {
                throw new SecurityException("Brak uprawnień do przeglądania tego udostępnionego zadania.");
            }
        }

        return taskMapper.toDto(share.getTask());
    }

    @Transactional(readOnly = true)
    public List<ShareTokenDTO> findActiveSharesByTaskId(Long taskId, String ownerUsername) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Zadanie nie istnieje: " + taskId));

        User owner = userRepository.findByLogin(ownerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + ownerUsername));

        boolean isAdmin = owner.getRole() == Role.ADMIN;
        boolean isAuthor = task.getAuthor() != null && task.getAuthor().getLogin().equals(ownerUsername);

        if (!isAdmin && !isAuthor) {
            throw new SecurityException("Brak dostępu do linków udostępnienia tego zadania");
        }

        return shareRepository.findByTaskId(taskId).stream()
                .filter(share -> !share.isExpired())
                .map(share -> new ShareTokenDTO(
                        share.getToken(),
                        taskId,
                        task.getTitle(),
                        share.getRecipient() != null ? share.getRecipient().getLogin() : null,
                        "/share/" + share.getToken()
                ))
                .collect(Collectors.toList());
    }
}
