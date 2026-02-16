package sk.tany.rest.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PageSerializationAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new PageSerializationAdvice())
                .build();
    }

    @Test
    void testPageSerialization() throws Exception {
        mockMvc.perform(get("/test/page")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").value("test"))
                .andExpect(jsonPath("$.totalPages").value(10))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.sort.sorted").value(true))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }

    @Test
    void testResponseEntityPageSerialization() throws Exception {
        mockMvc.perform(get("/test/response-entity-page")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]").value("test-re"))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/page")
        public Page<String> getPage() {
            return new PageImpl<>(List.of("test"), PageRequest.of(0, 10, Sort.by("id")), 100);
        }

        @GetMapping("/test/response-entity-page")
        public ResponseEntity<Page<String>> getResponseEntityPage() {
            return ResponseEntity.ok(new PageImpl<>(List.of("test-re"), PageRequest.of(0, 10), 10));
        }
    }
}
