package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.dto.client.blog.get.BlogClientGetResponse;
import sk.tany.rest.api.mapper.BlogClientApiMapper;
import sk.tany.rest.api.service.client.BlogClientService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogClientControllerTest {

    @Mock
    private BlogClientService blogService;

    @Mock
    private BlogClientApiMapper blogClientApiMapper;

    @InjectMocks
    private BlogClientController blogClientController;

    @Test
    void getAll() {
        List<BlogDto> blogs = Collections.singletonList(new BlogDto());
        when(blogService.getAll()).thenReturn(blogs);

        List<BlogDto> response = blogClientController.getAll();

        assertEquals(blogs, response);
    }

    @Test
    void getBlog_WhenFound() {
        String blogId = "1";
        BlogDto blogDto = new BlogDto();
        BlogClientGetResponse responseDto = new BlogClientGetResponse();

        when(blogService.getBlog(blogId)).thenReturn(Optional.of(blogDto));
        when(blogClientApiMapper.toGetResponse(blogDto)).thenReturn(responseDto);

        ResponseEntity<BlogClientGetResponse> response = blogClientController.getBlog(blogId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getBlog_WhenNotFound() {
        String blogId = "1";

        when(blogService.getBlog(blogId)).thenReturn(Optional.empty());

        ResponseEntity<BlogClientGetResponse> response = blogClientController.getBlog(blogId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getBlogBySlug_WhenFound() {
        String slug = "test-blog";
        BlogDto blogDto = new BlogDto();
        BlogClientGetResponse responseDto = new BlogClientGetResponse();

        when(blogService.getBlogBySlug(slug)).thenReturn(Optional.of(blogDto));
        when(blogClientApiMapper.toGetResponse(blogDto)).thenReturn(responseDto);

        ResponseEntity<BlogClientGetResponse> response = blogClientController.getBlogBySlug(slug);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getBlogBySlug_WhenNotFound() {
        String slug = "non-existent-blog";

        when(blogService.getBlogBySlug(slug)).thenReturn(Optional.empty());

        ResponseEntity<BlogClientGetResponse> response = blogClientController.getBlogBySlug(slug);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
