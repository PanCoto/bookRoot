package pl.studyshare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import pl.studyshare.dto.SortPreferences;
import pl.studyshare.enums.SortCriteria;
import org.springframework.data.domain.Sort;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldSaveSortPreferencesToCookieAndSessionWhenParamsProvided() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("sortField", "title")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("sortPrefs"))
                .andExpect(request().sessionAttribute("sortPrefs", new SortPreferences(SortCriteria.TITLE, Sort.Direction.ASC)))
                .andExpect(model().attribute("sortField", "title"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    public void shouldReadSortPreferencesFromCookieWhenNoParamsProvided() throws Exception {
        SortPreferences prefs = new SortPreferences(SortCriteria.POPULARITY, Sort.Direction.ASC);
        String json = objectMapper.writeValueAsString(prefs);
        String encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8);
        Cookie cookie = new Cookie("sortPrefs", encodedJson);

        mockMvc.perform(get("/tasks")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("sortPrefs", prefs))
                .andExpect(model().attribute("sortField", "popularity"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    public void shouldReadSortPreferencesFromSessionWhenNoParamsAndNoCookie() throws Exception {
        SortPreferences prefs = new SortPreferences(SortCriteria.TITLE, Sort.Direction.DESC);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("sortPrefs", prefs);

        mockMvc.perform(get("/tasks")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sortField", "title"))
                .andExpect(model().attribute("sortDir", "desc"));
    }

    @Test
    public void shouldFallbackToDefaultsWhenNoParamsNoCookieAndNoSession() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sortField", "createdDate"))
                .andExpect(model().attribute("sortDir", "desc"));
    }
}
