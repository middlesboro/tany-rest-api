package sk.tany.rest.api.service.common;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.service.common.enums.ImageKitType;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageKitServiceTest {

    @Mock
    private ImageKit imageKit;

    private ImageKitService imageKitService;

    @BeforeEach
    void setUp() {
        imageKitService = new ImageKitService(imageKit);
    }

    @Test
    void upload_ShouldSetFolderForBlog() throws Exception {
        byte[] fileContent = "test content".getBytes(StandardCharsets.UTF_8);
        String fileName = "test.jpg";
        Result mockResult = new Result();
        mockResult.setUrl("http://example.com/test.jpg");

        when(imageKit.upload(any(FileCreateRequest.class))).thenReturn(mockResult);

        imageKitService.upload(fileContent, fileName, ImageKitType.BLOG);

        ArgumentCaptor<FileCreateRequest> captor = ArgumentCaptor.forClass(FileCreateRequest.class);
        verify(imageKit).upload(captor.capture());

        FileCreateRequest capturedRequest = captor.getValue();
        assertEquals("blogs", capturedRequest.getFolder());
    }

    @Test
    void upload_ShouldSetFolderForProduct() throws Exception {
        byte[] fileContent = "test content".getBytes(StandardCharsets.UTF_8);
        String fileName = "test.jpg";
        Result mockResult = new Result();
        mockResult.setUrl("http://example.com/test.jpg");

        when(imageKit.upload(any(FileCreateRequest.class))).thenReturn(mockResult);

        imageKitService.upload(fileContent, fileName, ImageKitType.PRODUCT);

        ArgumentCaptor<FileCreateRequest> captor = ArgumentCaptor.forClass(FileCreateRequest.class);
        verify(imageKit).upload(captor.capture());

        FileCreateRequest capturedRequest = captor.getValue();
        assertEquals("products", capturedRequest.getFolder());
    }
}
