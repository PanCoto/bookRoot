package pl.studyshare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.studyshare.domain.User;
import pl.studyshare.dto.UserDTO;
import pl.studyshare.dto.UserUpdateRequest;
import pl.studyshare.enums.Role;
import pl.studyshare.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + id));
        return toDto(user);
    }

    public UserDTO updateUserProfile(Long id, UserUpdateRequest request, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + id));

        User currentUser = userRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Zalogowany użytkownik nie istnieje"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = user.getLogin().equals(currentUsername);

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Brak uprawnień do edycji tego profilu");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAge(request.getAge());
        
        String cleanEmail = (request.getEmail() != null && !request.getEmail().isBlank()) ? request.getEmail().trim() : null;
        user.setEmail(cleanEmail);

        String cleanDisplayName = (request.getDisplayName() != null && !request.getDisplayName().isBlank()) ? request.getDisplayName().trim() : null;
        user.setDisplayName(cleanDisplayName);
        
        user.setAnonymousMode(request.getAnonymousMode() != null ? request.getAnonymousMode() : false);

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public void changeUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + id));
        user.setRole(role);
        userRepository.save(user);
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + id));
        user.setEnabled(false);
        userRepository.save(user);
    }

    private UserDTO toDto(User u) {
        return new UserDTO(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getLogin(),
                u.getAge(),
                u.getRole(),
                u.getEnabled() != null && u.getEnabled(),
                u.getEmail(),
                u.getCreatedAt()
        );
    }

    public void changePassword(Long userId, pl.studyshare.dto.ChangePasswordRequest request, String currentUsername) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Nowe hasło i potwierdzenie nie są identyczne");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + userId));

        User currentUser = userRepository.findByLogin(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Zalogowany użytkownik nie istnieje"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = user.getLogin().equals(currentUsername);

        if (!isAdmin && !isOwner) {
            throw new SecurityException("Brak uprawnień do zmiany hasła tego użytkownika");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Aktualne hasło jest niepoprawne");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}