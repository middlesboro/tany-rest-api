package sk.tany.rest.api.domain.blog;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogRepositoryTest {

    @Mock
    private Nitrite nitrite;
    @Mock
    private ObjectRepository<Blog> objectRepository;
    @Mock
    private Cursor<Blog> cursor;

    private BlogRepository blogRepository;

    @BeforeEach
    void setUp() {
        when(nitrite.getRepository(Blog.class)).thenReturn(objectRepository);
        when(objectRepository.find()).thenReturn(cursor);

        // Prepare data
        Blog blog1 = new Blog();
        blog1.setId("1");
        blog1.setVisible(true);
        blog1.setOrder(2);

        Blog blog2 = new Blog();
        blog2.setId("2");
        blog2.setVisible(true);
        blog2.setOrder(1);

        Blog blog3 = new Blog();
        blog3.setId("3");
        blog3.setVisible(false);
        blog3.setOrder(0);

        when(cursor.iterator()).thenReturn(List.of(blog1, blog2, blog3).iterator());

        blogRepository = new BlogRepository(nitrite);
        blogRepository.init();
    }

    @Test
    void findAllByVisibleTrue_shouldReturnSortedVisibleBlogs() {
        List<Blog> result = blogRepository.findAllByVisibleTrue();

        assertEquals(2, result.size());
        assertEquals("2", result.get(0).getId()); // Order 1
        assertEquals("1", result.get(1).getId()); // Order 2
    }
}
