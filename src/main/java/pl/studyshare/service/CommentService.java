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

    @Transactional(readOnly = true)
    public List<CommentDTO> findCommentsByAnswerId(Long answerId) {
        return commentRepository.findByAnswerIdOrderByCreatedDateAsc(answerId).stream()
                .map(comment -> new CommentDTO(
                        comment.getId(),
                        comment.getContent(),
                        comment.getCreatedDate(),
                        comment.getAuthor() != null ? comment.getAuthor().getLogin() : "Anonim"
                ))
                .collect(Collectors.toList());
    }
}
