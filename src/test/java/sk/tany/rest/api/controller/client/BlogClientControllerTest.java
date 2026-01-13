package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.service.client.BlogClientService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogClientControllerTest {

    @Mock
    private BlogClientService blogService;

    @InjectMocks
    private BlogClientController blogClientController;

    @Test
    void getAll() {
        List<BlogDto> blogs = Collections.singletonList(new BlogDto());
        when(blogService.getAll()).thenReturn(blogs);

        List<BlogDto> response = blogClientController.getAll();

        assertEquals(blogs, response);
    }
}
