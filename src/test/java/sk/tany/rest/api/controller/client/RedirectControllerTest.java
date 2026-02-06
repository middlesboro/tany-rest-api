package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RedirectControllerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private RedirectController redirectController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(redirectController).build();
    }

    @Test
    void redirectContent_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/content/123-some-slug"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/some-slug"));
    }

    @Test
    void redirectBlog_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/blog/some-blog-slug-b456.html"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/blog/some-blog-slug"));
    }

    @Test
    void redirectCategoryOrBrand_FoundBySlug_ShouldRedirect() throws Exception {
        Category category = new Category();
        category.setSlug("found-slug");
        when(categoryRepository.findBySlug("found-slug")).thenReturn(Optional.of(category));

        mockMvc.perform(get("/123-found-slug"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/kategoria/found-slug"));
    }

    @Test
    void redirectCategoryOrBrand_FoundByPrestashopId_ShouldRedirect() throws Exception {
        Category category = new Category();
        category.setSlug("category-slug");
        when(categoryRepository.findBySlug("unknown-slug")).thenReturn(Optional.empty());
        when(categoryRepository.findByPrestashopId(123L)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/123-unknown-slug"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/kategoria/category-slug"));
    }

    @Test
    void redirectCategoryOrBrand_FoundByBrandPrestashopId_ShouldRedirect() throws Exception {
        Brand brand = new Brand();
        brand.setName("MyBrand");
        when(categoryRepository.findBySlug("unknown-slug")).thenReturn(Optional.empty());
        when(categoryRepository.findByPrestashopId(123L)).thenReturn(Optional.empty());
        when(brandRepository.findByPrestashopId(123L)).thenReturn(Optional.of(brand));

        mockMvc.perform(get("/123-unknown-slug"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/kategoria/vsetky-produkty?q=Brand-MyBrand"));
    }

    @Test
    void redirectCategoryOrBrand_NotFound_ShouldReturn404() throws Exception {
        when(categoryRepository.findBySlug("unknown-slug")).thenReturn(Optional.empty());
        when(categoryRepository.findByPrestashopId(123L)).thenReturn(Optional.empty());
        when(brandRepository.findByPrestashopId(123L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/123-unknown-slug"))
                .andExpect(status().isNotFound());
    }
}
