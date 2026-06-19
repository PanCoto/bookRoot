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
        Category operatingSystems = categoryRepository.save(
                new Category("Systemy Operacyjne", "Pytania z wykładów z Systemów Operacyjnych"));

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

        seedOperatingSystemsTasks(operatingSystems, admin, moderator, student1, student2);
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


    private void seedOperatingSystemsTasks(Category category, User admin, User moderator,
                                            User student1, User student2) {

        createApprovedTask(
                "Systemy Operacyjne – Wykład 1: Wprowadzenie",
                """
                # Wykład 1: Wprowadzenie do systemów operacyjnych

                1. Jaka jest różnica pomiędzy systemem czasu rzeczywistego *hard real time*, \
                a *soft real time*?
                2. Opisz, jak wygląda praca wsadowa z perspektywy użytkownika systemu.
                3. Jaka jest różnica między jednoprogramowym systemem wsadowym, a wieloprogramowym \
                systemem wsadowym?
                4. Dlaczego powłoka (shell) w Linuksie nie jest uważana za część jądra (kernel) systemu?
                """,
                category, admin, student1);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 2: Tryby pracy procesora i wejście/wyjście",
                """
                # Wykład 2: Tryby pracy procesora i wejście/wyjście

                1. Czy wpadnięcie w nieskończoną pętlę zatrzyma system operacyjny Unix/Linux?
                2. Podaj przykłady instrukcji uprzywilejowanych.
                3. Podaj przykład wyjaśniający problem spójności pamięci podręcznej (cache).
                4. Jaka jest różnica między `sync` oraz `async` w we/wy (IO)?
                5. Wskaż najszybszą i najwolniejszą metodę spośród trzech sposobów komunikacji \
                z urządzeniami IO.
                6. Podaj przykład procesu wykonującego się w trybie użytkownika oraz procesu \
                wykonującego się w trybie jądra.
                7. Co to jest przełączenie kontekstu (*context switch*)?
                8. Podaj przykład niepowodzenia (*fault*).
                """,
                category, moderator, student2);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 3: Procesy i wątki",
                """
                # Wykład 3: Procesy i wątki

                1. Czym różni się proces od programu?
                2. Czy możliwe jest przejście ze stanu (procesu) Oczekującego do Aktywnego? \
                Jeśli tak, podaj scenariusz przejścia.
                3. Podaj przykład sytuacji, w której proces będący w stanie aktywnym przechodzi \
                w stan oczekujący.
                4. **Podaj funkcję planisty (długo-/krótko-/średnioterminowego).**
                5. Który planista jest najważniejszy i musi istnieć zawsze?
                6. Z dwóch operacji: przełączenie kontekstu i przełączenie trybu w systemie \
                operacyjnym zaprojektowanym z myślą o procesorach bez błędów itp., szybsze jest: …
                7. Co oznacza kod powrotu z funkcji `fork()` (3 możliwości)?
                8. Wymień 2 zalety wielowątkowości.
                9. Dwa wątki zostały stworzone wewnątrz jednego procesu, wykonują funkcję `f`, \
                a `x` jest zmienną lokalną typu auto. Jeżeli najpierw wątek pierwszy wykona \
                przypisanie `x = 1`, a później wątek drugi wykona przypisanie `x = 2`, to później \
                wątek pierwszy, odczytując zmienną `x`, odczyta …
                """,
                category, admin, student1);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 4: Sekcja krytyczna i synchronizacja",
                """
                # Wykład 4: Sekcja krytyczna i synchronizacja

                1. Kiedy mówimy o sytuacji wyścigu?
                2. Opisz warunki, jakie powinno spełniać rozwiązanie problemu sekcji krytycznej.
                3. Jak działa operacja `s.wait()`, gdzie `s` jest semaforem zliczającym?
                4. Podaj przykład zakleszczenia (wraz z definicją).
                5. Podaj definicję zakleszczenia.
                6. Podaj przykład zagłodzenia (*starvation*).
                7. Wyspecyfikuj problem czytelników i pisarzy.
                """,
                category, moderator, student2);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 5: Monitory i komunikacja międzyprocesowa",
                """
                # Wykład 5: Monitory i komunikacja międzyprocesowa

                1. Podaj różnice pomiędzy blokującą a nieblokującą operacją odbioru komunikatu.
                2. Wymień 2 czynności, jakie wykonuje operacja `C.wait()`.
                3. Jakie są ograniczenia obiektu w Javie traktowanego jako monitor?
                4. Korzystając z biblioteki POSIX Threads, zaimplementuj monitor z 3 zmiennymi \
                warunkowymi. Ile potrzeba do tego zmiennych typu `sem_t`, ile zmiennych \
                `pthread_mutex_t`, a ile zmiennych `pthread_cond_t`?

                ## Pytania dodatkowe
                - Który wątek uruchomi się pierwszy po `wait`/`signal` (semantyka Mesa czy Hoare'a)?
                - Co robi operacja `wait` w wątkach?
                - Jakie są argumenty funkcji `cond_wait`?
                - Co nie jest zmienną warunkową w monitorze?
                """,
                category, admin, student1);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 6: Szeregowanie procesów",
                """
                # Wykład 6: Szeregowanie procesów

                1. Na czym polega postarzanie (*aging*) w szeregowaniu procesów?
                2. Który znany Ci algorytm szeregowania zastosowałbyś w systemie z podziałem czasu?
                3. Dlaczego w systemie z ochroną nie stosuje się planowania bez wywłaszczeń?
                4. Co to jest wywłaszczenie (*preemption*)?
                5. Narysuj trzy diagramy Gantta ilustrujące działanie CPU według trzech algorytmów:
                   - FCFS (*First Come First Served*),
                   - SJF (*Shortest Job First*) z wywłaszczeniem i bez,
                   - Round Robin (planowanie rotacyjne).

                ## Pytania dodatkowe
                - Czy planowanie bez wywłaszczeń może być dobrą strategią?
                - Czym różnią się systemy interakcyjne, wsadowe i czasu rzeczywistego?
                """,
                category, moderator, student2);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 7: Zarządzanie pamięcią",
                """
                # Wykład 7: Zarządzanie pamięcią

                1. Kiedy mówimy o fragmentacji wewnętrznej?
                2. W jakim znanym Ci mechanizmie zarządzania przestrzenią fragmentacja wewnętrzna \
                jest najbardziej dotkliwa?
                3. Co to jest kompakcja (*compaction*)?
                4. Jaka jest funkcja bufora translacji adresów (TLB)?
                5. Jaka jest funkcja bitu trybu jądra w pozycji tablicy stron?
                6. Dlaczego w praktycznych realizacjach stosuje się stronicowanie 2- lub 3-poziomowe?
                """,
                category, admin, student1);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 8: Stronicowanie i zastępowanie stron",
                """
                # Wykład 8: Stronicowanie i zastępowanie stron

                1. **Zasymuluj zastępowanie stron (algorytm stronicowania): FIFO, zegarowy, \
                optymalny, LRU, NFU itd.**
                2. Co to jest szamotanie (*thrashing*)?
                3. Jak działa postarzanie (*aging*) w algorytmie NFU?
                4. Jaka jest różnica pomiędzy algorytmami FIFO a algorytmem drugiej szansy?
                5. Co to jest anomalia Belady'ego?
                6. Omów znaczenie bitów D (*Dirty*) i R (*Reference*) w tablicy stron.
                """,
                category, moderator, student2);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 9: Dyski i RAID",
                """
                # Wykład 9: Dyski i RAID

                1. **Mając dany ciąg żądań, podaj kolejność ich obsługi przez dysk według \
                wskazanego algorytmu (SSTF, SCAN lub C-LOOK).**
                2. Jaka jest różnica między macierzą RAID 4 a RAID 5?
                3. Dlaczego RAID 0 nie jest uważany za prawdziwą macierz RAID?
                4. Dlaczego klasyczne algorytmy szeregowania dysku (np. SSTF) nie mają sensu \
                w przypadku dysków SSD?
                5. **Wymień składowe czasu operacji dysku HDD — które z nich są dominujące, \
                a które bardzo małe?**
                6. Co to jest dostęp surowy do dysku, kiedy jest wykorzystywany — podaj jeden \
                przykład.
                """,
                category, admin, student1);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 10: Systemy plików",
                """
                # Wykład 10: Systemy plików

                1. Jakiej struktury danych użyłbyś, aby sprawdzić, czy blok dyskowy o numerze i \
                z dysku o numerze J jest w pamięci podręcznej dysku:
                   - A) lista na wskaźnikach,
                   - B) drzewo czerwono-czarne,
                   - C) tablica mieszająca?
                2. Wiadomo, że na dysku HDD im większy rozmiar bloku, tym większa średnia prędkość \
                transmisji danych. Dlaczego rzadko (albo wcale) spotyka się systemy plików \
                o rozmiarze bloku np. 16 MB?
                3. Jak działa strategia prealokacji bloków dyskowych i w przypadku jakich typów \
                pamięci masowej się sprawdza?
                4. Plik w UNIX ma 2 KB. Ile bloków indeksowych potrzebuje ten plik?
                5. Podaj, gdzie przechowywane są atrybuty plików w systemie MS-DOS, a gdzie w UNIX.
                6. Która metoda alokacji danych oferuje najwyższą wydajność operacji `seek()`, \
                a która najmniejszą?
                7. Dlaczego odczyt pliku odwzorowanego w pamięci (`mmap`) jest szybszy od \
                klasycznego wywoływania funkcji `read()`?

                ## Pytania dodatkowe
                - Co odczytuje funkcja C `mmap()`?
                """,
                category, moderator, student2);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 11: Bezpieczeństwo i uprawnienia",
                """
                # Wykład 11: Bezpieczeństwo i uprawnienia

                1. Podaj wadę i zaletę kryptografii asymetrycznej w porównaniu z symetryczną.
                2. Jak działa bit SUID dla plików wykonywalnych w UNIX?
                3. Jakiego uprawnienia potrzebujesz, aby usunąć plik w UNIX?
                4. Wyjaśnij, w którym momencie podczas otwarcia pliku funkcją `open()` i jego \
                odczytu funkcją `read()` wykorzystywane są listy dostępu i listy uprawnień.
                5. Czy bezpieczeństwo absolutne jest możliwe do osiągnięcia? Odpowiedź uzasadnij.
                """,
                category, admin, student1);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 12: Ataki i podatności",
                """
                # Wykład 12: Ataki i podatności

                1. Omów, jak działa atak wykorzystujący przepełnienie bufora.
                2. Omów, jak działa atak polegający na wstrzyknięciu kodu.
                3. Dlaczego wprowadzony przez producentów procesorów bit *execute disable* nie \
                likwiduje wszystkich problemów związanych z przepełnieniem bufora?
                4. Podaj przykład ataku DoS (lub DDoS) wykorzystującego podatność sprzętową albo \
                podatność programową.
                """,
                category, moderator, student2);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 13: Systemy wieloprocesorowe",
                """
                # Wykład 13: Systemy wieloprocesorowe

                1. Co to jest fałszywe współdzielenie (*false sharing*) w systemach \
                wieloprocesorowych?
                2. Wyjaśnij, na czym polega problem utrzymywania spójności pamięci podręcznej \
                w systemie, gdzie każdy procesor ma własną pamięć podręczną (cache).
                3. Dlaczego znany z klasycznego UNIX-a model synchronizacji oparty o \
                niewywłaszczalne jądro i maskowanie przerwań nie sprawdzi się w systemach \
                wieloprocesorowych?
                4. Dlaczego ciężko zbudować procesor o zegarze 30 GHz?
                """,
                category, admin, student1);

        createApprovedTask(
                "Systemy Operacyjne – Wykład 14: Systemy czasu rzeczywistego",
                """
                # Wykład 14: Systemy czasu rzeczywistego

                1. Dlaczego system uniksowy, zgodnie ze standardem POSIX 1.b, nie jest systemem \
                klasy *hard real time*?
                2. Podaj warunek konieczny wykonalności szeregowania w systemie czasu \
                rzeczywistego: `U = Σ(Cᵢ/Tᵢ) ≤ 1`, wyjaśniając symbole `Cᵢ` (czas wykonania \
                zadania) i `Tᵢ` (okres zadania).
                3. Dlaczego system operacyjny z niewywłaszczalnym jądrem ma bardzo wysoki czas \
                reakcji na zdarzenie?
                4. Podaj przykład współczesnego systemu komputerowego, wykorzystywanego \
                powszechnie w Polsce i na świecie, który wykorzystuje planowanie długoterminowe.
                5. Co to jest klaster? Podaj definicję.
                6. Omów różnice pomiędzy sieciowym systemem operacyjnym a rozproszonym systemem \
                operacyjnym.
                7. Omów planowanie EDF (*Earliest Deadline First*) ze szczególnym uwzględnieniem \
                algorytmu SCAN.
                8. **Omów szeregowanie dysku.**
                """,
                category, moderator, student2);
    }
}