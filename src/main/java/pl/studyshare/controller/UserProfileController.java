package pl.studyshare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.studyshare.domain.User;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.repository.CommentRepository;
import pl.studyshare.repository.TaskRepository;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.repository.AnswerRepository;

@Controller
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;

    @GetMapping("/users/{login}")
    public String viewProfile(@PathVariable String login,
                              @AuthenticationPrincipal UserDetails currentUser,
                              Model model) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje."));


        boolean isOwner = currentUser != null && currentUser.getUsername().equals(login);
        boolean isAdmin = currentUser != null && currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (Boolean.TRUE.equals(user.getAnonymousMode()) && !isOwner && !isAdmin) {
            throw new SecurityException("Ten profil jest prywatny (użytkownik korzysta z trybu anonimowego).");
        }

        int totalReputation = answerRepository.sumUpvotesByAuthorLogin(login);
        var userTasks = taskRepository.findByAuthorLoginAndStatusOrderByCreatedDateDesc(login, TaskStatus.APPROVED);
        var userComments = commentRepository.findByAuthorLoginAndAnonymousFalseOrderByCreatedDateDesc(login);
        var userAnswers = answerRepository.findByAuthorLoginAndAnonymousFalseOrderByCreatedDateDesc(login);

        model.addAttribute("profileUser", user);
        model.addAttribute("reputation", totalReputation);
        model.addAttribute("tasks", userTasks);
        model.addAttribute("comments", userComments);
        model.addAttribute("answers", userAnswers);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isAdmin", isAdmin);

        return "profile-view";
    }
}
