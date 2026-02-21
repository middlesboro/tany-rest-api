package sk.tany.rest.api.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.dto.PageContentDto;
import sk.tany.rest.api.dto.admin.pagecontent.get.PageContentAdminGetResponse;
import sk.tany.rest.api.mapper.PageContentAdminApiMapper;
import sk.tany.rest.api.service.admin.PageContentAdminService;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PageContentAdminController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PageContentAdminApiMapper.class)
public class PageContentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PageContentAdminService pageContentService;

    @MockitoBean
    private PageContentAdminApiMapper apiMapper;

    @MockitoBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPage_shouldReturnCreated() throws Exception {
        when(pageContentService.save(any())).thenReturn(new PageContentDto());
        // Since apiMapper is mocked, we should probably mock its methods too or rely on returning null/defaults if not strictly checked by controller return logic,
        // but creating a response object usually happens.
        // However, the test that failed was getPage_shouldReturnOk.

        mockMvc.perform(post("/api/admin/pages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPages_shouldReturnOk() throws Exception {
        Page<PageContentDto> page = new PageImpl<>(Collections.emptyList());
        when(pageContentService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/pages"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPage_shouldReturnOk() throws Exception {
        when(pageContentService.findById("1")).thenReturn(Optional.of(new PageContentDto()));
        when(apiMapper.toGetResponse(any())).thenReturn(new PageContentAdminGetResponse());

        mockMvc.perform(get("/api/admin/pages/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePage_shouldReturnOk() throws Exception {
        when(pageContentService.update(any(), any())).thenReturn(new PageContentDto());

        mockMvc.perform(put("/api/admin/pages/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePage_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/pages/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
