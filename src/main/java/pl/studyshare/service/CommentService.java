package pl.studyshare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.Answer;
import pl.studyshare.domain.Comment;
import pl.studyshare.domain.User;
import pl.studyshare.dto.CommentDTO;
import pl.studyshare.repository.AnswerRepository;
import pl.studyshare.repository.CommentRepository;
import pl.studyshare.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import pl.studyshare.enums.Role;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public void addComment(Long answerId, String content, String authorUsername) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Komentarz nie może być pusty");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("Komentarz może mieć maksymalnie 500 znaków");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Odpowiedź nie istnieje: " + answerId));

        User author = userRepository.findByLogin(authorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + authorUsername));

        Comment comment = Comment.builder()
                .content(content.trim())
                .createdDate(LocalDate.now())
                .answer(answer)
                .author(author)
                .build();

        commentRepository.save(comment);
    }

    public CommentDTO addComment(Long answerId, String content, boolean anonymous, String authorUsername) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Komentarz nie może być pusty");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("Komentarz może mieć maksymalnie 1000 znaków");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Odpowiedź nie istnieje: " + answerId));

        User author = userRepository.findByLogin(authorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + authorUsername));

        Comment comment = Comment.builder()
                .content(content.trim())
                .anonymous(anonymous)
                .createdDate(LocalDate.now())
                .answer(answer)
                .author(author)
                .build();

        Comment saved = commentRepository.save(comment);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findCommentsByAnswerId(Long answerId) {
        return commentRepository.findByAnswerIdOrderByCreatedDateAsc(answerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findByAnswerId(Long answerId) {
        return findCommentsByAnswerId(answerId);
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Komentarz nie istnieje: " + commentId));

        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + username));

        boolean isAdmin  = user.getRole() == Role.ADMIN;
        boolean isAuthor = comment.getAuthor() != null && comment.getAuthor().getLogin().equals(username);

        if (!isAdmin && !isAuthor) {
            throw new SecurityException("Brak uprawnień do usunięcia tego komentarza");
        }
        commentRepository.delete(comment);
    }

    private CommentDTO toDto(Comment c) {
        String authorName = (Boolean.TRUE.equals(c.getAnonymous()) || c.getAuthor() == null) ? null : c.getAuthor().getLogin();
        return new CommentDTO(c.getId(), c.getContent(), c.getCreatedDate(), authorName);
    }

    @Transactional(readOnly = true)
    public List<Long> getDeletableCommentIds(List<Long> answerIds, String username) {
        if (username == null || answerIds == null || answerIds.isEmpty()) {
            return List.of();
        }
        return answerIds.stream()
                .flatMap(aid -> commentRepository.findByAnswerIdOrderByCreatedDateAsc(aid).stream())
                .filter(c -> c.getAuthor() != null && username.equals(c.getAuthor().getLogin()))
                .map(Comment::getId)
                .collect(Collectors.toList());
    }
}
