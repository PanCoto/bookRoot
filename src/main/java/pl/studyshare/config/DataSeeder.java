package pl.studyshare.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.*;
import pl.studyshare.enums.Role;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.repository.CategoryRepository;
import pl.studyshare.repository.TaskRepository;
import pl.studyshare.repository.UserRepository;

import java.time.LocalDate;

@Component
@Profile({"dev", "postgres", "docker"})
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      TaskRepository taskRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Category programming = categoryRepository.save(
                new Category("Programowanie", "Przedmioty programistyczne"));
        Category algorithms = categoryRepository.save(
                new Category("Algorytmy", "Algorytmy i struktury danych"));
        Category databases = categoryRepository.save(
                new Category("Bazy danych", "Bazy danych i SQL"));

        User admin = User.builder()
                .login("admin")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("Systemu")
                .age(30)
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        User moderator = User.builder()
                .login("moderator")
                .password(passwordEncoder.encode("mod123"))
                .firstName("Moderator")
                .lastName("Systemu")
                .age(25)
                .role(Role.MODERATOR)
                .enabled(true)
                .build();
        userRepository.save(moderator);

        User student1 = createStudent("studentjan", "pass123", "Jan", "Kowalski", 20);
        User student2 = createStudent("studentanna", "pass123", "Anna", "Nowak", 21);

        createApprovedTask("Zadanie 1: Sortowanie przez scalanie",
                "Opisz działanie algorytmu merge sort i jego złożoność obliczeniową...",
                algorithms, admin, student1);

        createApprovedTask("Zadanie 2: Normalizacja bazy danych",
                "Wyjaśnij pojęcia 1NF, 2NF, 3NF na przykładzie tabeli...",
                databases, admin, student2);

        createApprovedTask("Zadanie 3: Dziedziczenie w Javie",
                "Napisz przykład dziedziczenia klas w języku Java...",
                programming, admin, student1);

        createPendingTask("Kolokwium: Drzewa binarne",
                "Zaimplementuj przechodzenie drzewa BST...", algorithms, student1);

        createPendingTask("Test: Zapytania SQL",
                "Napisz zapytanie SELECT z grupowaniem i JOIN...", databases, student2);
    }

    private User createStudent(String login, String rawPassword,
                               String firstName, String lastName, int age) {
        User user = User.builder()
                .login(login)
                .password(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .age(age)
                .role(Role.USER)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    private void createApprovedTask(String title, String content,
                                    Category category, User approvedBy, User author) {
        Task task = Task.builder()
                .title(title)
                .content(content)
                .status(TaskStatus.APPROVED)
                .createdDate(LocalDate.now())
                .author(author)
                .category(category)
                .approvedBy(approvedBy)
                .anonymous(false)
                .build();
        taskRepository.save(task);
    }

    private void createPendingTask(String title, String content,
                                   Category category, User author) {
        Task task = Task.builder()
                .title(title)
                .content(content)
                .status(TaskStatus.PENDING)
                .createdDate(LocalDate.now())
                .author(author)
                .category(category)
                .anonymous(true)
                .build();
        taskRepository.save(task);
    }
}
