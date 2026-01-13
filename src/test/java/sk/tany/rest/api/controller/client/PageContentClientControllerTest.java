package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.dto.PageContentDto;
import sk.tany.rest.api.dto.client.pagecontent.get.PageContentClientGetResponse;
import sk.tany.rest.api.mapper.PageContentClientApiMapper;
import sk.tany.rest.api.service.client.PageContentClientService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PageContentClientController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for client test if public
@ActiveProfiles("test")
public class PageContentClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageContentClientService pageContentService;

    @MockBean
    private PageContentClientApiMapper apiMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void getPageBySlug_shouldReturnOk() throws Exception {
        when(pageContentService.findBySlug("test-slug")).thenReturn(Optional.of(new PageContentDto()));
        when(apiMapper.toGetResponse(any())).thenReturn(new PageContentClientGetResponse());

        mockMvc.perform(get("/api/pages/test-slug"))
                .andExpect(status().isOk());
    }

    @Test
    void getPageBySlug_shouldReturnNotFound() throws Exception {
        when(pageContentService.findBySlug("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pages/unknown"))
                .andExpect(status().isNotFound());
    }
}
