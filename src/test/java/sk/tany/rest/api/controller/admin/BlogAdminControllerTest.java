package sk.tany.rest.api.controller.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.service.admin.BlogAdminService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogAdminControllerTest {

    @Mock
    private BlogAdminService blogService;

    @InjectMocks
    private BlogAdminController blogAdminController;

    @Test
    void createBlog() {
        BlogDto blogDto = new BlogDto();
        blogDto.setTitle("Test Blog");
        when(blogService.save(blogDto)).thenReturn(blogDto);

        ResponseEntity<BlogDto> response = blogAdminController.createBlog(blogDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(blogDto, response.getBody());
    }

    @Test
    void getBlogs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BlogDto> page = new PageImpl<>(Collections.emptyList());
        when(blogService.findAll(pageable)).thenReturn(page);

        Page<BlogDto> response = blogAdminController.getBlogs(pageable);

        assertEquals(page, response);
    }

    @Test
    void getBlog() {
        String id = "1";
        BlogDto blogDto = new BlogDto();
        blogDto.setId(id);
        when(blogService.findById(id)).thenReturn(Optional.of(blogDto));

        ResponseEntity<BlogDto> response = blogAdminController.getBlog(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(blogDto, response.getBody());
    }

    @Test
    void updateBlog() {
        String id = "1";
        BlogDto blogDto = new BlogDto();
        blogDto.setId(id);
        when(blogService.update(id, blogDto)).thenReturn(blogDto);

        ResponseEntity<BlogDto> response = blogAdminController.updateBlog(id, blogDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(blogDto, response.getBody());
    }

    @Test
    void deleteBlog() {
        String id = "1";
        doNothing().when(blogService).deleteById(id);

        ResponseEntity<Void> response = blogAdminController.deleteBlog(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(blogService).deleteById(id);
    }

    @Test
    void uploadImage() {
        String id = "1";
        MultipartFile file = mock(MultipartFile.class);
        BlogDto blogDto = new BlogDto();
        blogDto.setId(id);
        when(blogService.uploadImage(id, file)).thenReturn(blogDto);

        ResponseEntity<BlogDto> response = blogAdminController.uploadImage(id, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(blogDto, response.getBody());
    }

    @Test
    void deleteImage() {
        String id = "1";
        doNothing().when(blogService).deleteImage(id);

        ResponseEntity<Void> response = blogAdminController.deleteImage(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(blogService).deleteImage(id);
    }
}
