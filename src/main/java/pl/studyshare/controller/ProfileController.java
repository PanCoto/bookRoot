package pl.studyshare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.studyshare.domain.User;
import pl.studyshare.dto.UserUpdateRequest;
import pl.studyshare.dto.ChangePasswordRequest;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.repository.AnswerRepository;
import pl.studyshare.repository.TaskRepository;
import pl.studyshare.repository.CommentRepository;
import pl.studyshare.service.UserService;
import pl.studyshare.service.AvatarStorageService;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AvatarStorageService avatarStorageService;
    private final AnswerRepository answerRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;

    /**
     * Konwertuje puste stringi na null przed walidacją.
     * Pozwala to na opcjonalne pola jak email – @Email(null) przechodzi walidację.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String viewMyProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        
        int totalReputation = answerRepository.sumScoreByAuthorId(user.getId());
        var userTasks = taskRepository.findByAuthorLoginAndStatusOrderByCreatedDateDesc(user.getLogin(), TaskStatus.APPROVED);
        var userComments = commentRepository.findByAuthorLoginOrderByCreatedDateDesc(user.getLogin());
        var userAnswers = answerRepository.findByAuthorLoginAndAnonymousFalseOrderByCreatedDateDesc(user.getLogin());

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("profileUser", user);
        model.addAttribute("reputation", totalReputation);
        model.addAttribute("tasks", userTasks);
        model.addAttribute("comments", userComments);
        model.addAttribute("answers", userAnswers);
        model.addAttribute("isOwner", true);
        model.addAttribute("isAdmin", isAdmin);

        return "profile-view";
    }

    @GetMapping("/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();

        UserUpdateRequest profile = new UserUpdateRequest(
                user.getFirstName(),
                user.getLastName(),
                user.getAge(),
                user.getDisplayName(),
                user.getEmail(),
                user.getAnonymousMode() != null ? user.getAnonymousMode() : false,
                user.getAvatarFilename()
        );

        model.addAttribute("userProfile", profile);
        return "profile-edit";
    }

    @PostMapping("/edit/avatar")
    public String uploadAvatar(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("avatar") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Proszę wybrać plik do przesłania.");
            return "redirect:/profile/edit";
        }

        try {
            User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
            String filename = avatarStorageService.storeAvatar(file, user.getLogin());
            user.setAvatarFilename(filename);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Zdjęcie profilowe zostało zaktualizowane.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd zapisu pliku: " + e.getMessage());
        }
        return "redirect:/profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("userProfile") UserUpdateRequest request,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "profile-edit";
        }

        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAge(request.getAge());

        // Bezpieczne mapowanie pustych pól na null (zapobiega Unique Constraint Exception w bazie SQL)
        String cleanEmail = (request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail().trim() : null;
        user.setEmail(cleanEmail);

        String cleanDisplayName = (request.getDisplayName() != null && !request.getDisplayName().isBlank())
                ? request.getDisplayName().trim() : null;
        user.setDisplayName(cleanDisplayName);

        user.setAnonymousMode(request.getAnonymousMode() != null ? request.getAnonymousMode() : false);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", "Podany adres e-mail jest już zajęty przez innego użytkownika.");
            return "profile-edit";
        }

        return "redirect:/profile/edit?success";
    }

    @GetMapping("/password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest("", "", ""));
        return "profile-password";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "profile-password";
        }

        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        try {
            userService.changePassword(user.getId(), request, userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("currentPassword", "error.changePasswordRequest", e.getMessage());
            return "profile-password";
        }

        return "redirect:/profile/password?success";
    }
}