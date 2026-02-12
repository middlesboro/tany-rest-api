package sk.tany.rest.api.service.client.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.blog.Blog;
import sk.tany.rest.api.domain.blog.BlogRepository;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.mapper.BlogMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogClientServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private BlogMapper blogMapper;

    @InjectMocks
    private BlogClientServiceImpl blogClientService;

    @Test
    void getBlogBySlug_WhenFound() {
        String slug = "test-blog";
        Blog blog = new Blog();
        blog.setSlug(slug);
        BlogDto blogDto = new BlogDto();
        blogDto.setSlug(slug);

        when(blogRepository.findBySlug(slug)).thenReturn(Optional.of(blog));
        when(blogMapper.toDto(blog)).thenReturn(blogDto);

        Optional<BlogDto> result = blogClientService.getBlogBySlug(slug);

        assertTrue(result.isPresent());
        assertEquals(slug, result.get().getSlug());
    }

    @Test
    void getBlogBySlug_WhenNotFound() {
        String slug = "non-existent-blog";

        when(blogRepository.findBySlug(slug)).thenReturn(Optional.empty());

        Optional<BlogDto> result = blogClientService.getBlogBySlug(slug);

        assertTrue(result.isEmpty());
    }
}
