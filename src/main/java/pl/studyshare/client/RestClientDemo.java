package pl.studyshare.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.studyshare.dto.CategoryDTO;

import java.util.Arrays;

@Component
@Profile("dev")
public class RestClientDemo implements CommandLineRunner {

    private final RestClient restClient;

    public RestClientDemo(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            CategoryDTO[] categories = restClient.get()
                    .uri("/api/categories")
                    .retrieve()
                    .body(CategoryDTO[].class);

            if (categories != null) {
                System.out.println("=== REST Client Demo – kategorie ===");
                Arrays.stream(categories)
                        .forEach(c -> System.out.printf("  [%d] %s (%d zadań)%n",
                                c.id(), c.name(), c.taskCount()));
            }
        } catch (Exception e) {
            System.out.println("=== REST Client Demo – Błąd połączenia ===");
            System.out.println(e.getMessage());
        }
    }
}
