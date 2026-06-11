package pl.studyshare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setAge(request.age());

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
                u.getEnabled() != null ? u.getEnabled() : true
        );
    }
}
