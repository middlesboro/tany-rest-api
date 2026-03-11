package sk.tany.rest.api.controller.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageAdminControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageAdminController imageAdminController;

    @Test
    void uploadImage_ShouldReturnUrl() throws IOException {
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        String imageUrl = "http://image.url";

        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getBytes()).thenReturn(new byte[]{});
        when(imageService.upload(any(byte[].class), any(String.class), eq(ImageKitType.CATEGORY))).thenReturn(imageUrl);

        ResponseEntity<String> result = imageAdminController.uploadImage(file, ImageKitType.CATEGORY);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(imageUrl, result.getBody());
        verify(imageService, times(1)).upload(any(byte[].class), any(String.class), eq(ImageKitType.CATEGORY));
    }
}
