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
import sk.tany.rest.api.dto.admin.homepage.HomepageGridAdminDto;
import sk.tany.rest.api.dto.admin.homepage.patch.HomepageGridPatchRequest;
import sk.tany.rest.api.exception.HomepageGridException;
import sk.tany.rest.api.service.admin.HomepageGridAdminService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomepageGridAdminControllerTest {

    @Mock
    private HomepageGridAdminService homepageGridAdminService;

    @InjectMocks
    private HomepageGridAdminController homepageGridAdminController;

    @Test
    void getHomepageGrids_ShouldReturnPagedGrids() {
        Pageable pageable = PageRequest.of(0, 10);
        HomepageGridAdminDto gridDto = new HomepageGridAdminDto();
        gridDto.setTitle("Test Grid");
        Page<HomepageGridAdminDto> gridPage = new PageImpl<>(Collections.singletonList(gridDto));

        when(homepageGridAdminService.findAll(pageable)).thenReturn(gridPage);

        Page<HomepageGridAdminDto> result = homepageGridAdminController.getHomepageGrids(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Grid", result.getContent().get(0).getTitle());
        verify(homepageGridAdminService, times(1)).findAll(pageable);
    }

    @Test
    void getHomepageGrid_ShouldReturnGrid_WhenFound() {
        String id = "123";
        HomepageGridAdminDto gridDto = new HomepageGridAdminDto();
        gridDto.setId(id);
        gridDto.setTitle("Test Grid");

        when(homepageGridAdminService.findById(id)).thenReturn(Optional.of(gridDto));

        ResponseEntity<HomepageGridAdminDto> response = homepageGridAdminController.getHomepageGrid(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(gridDto, response.getBody());
    }

    @Test
    void getHomepageGrid_ShouldReturnNotFound_WhenNotFound() {
        String id = "123";

        when(homepageGridAdminService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<HomepageGridAdminDto> response = homepageGridAdminController.getHomepageGrid(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createHomepageGrid_ShouldReturnCreatedGrid() {
        HomepageGridAdminDto requestDto = new HomepageGridAdminDto();
        requestDto.setTitle("New Grid");
        HomepageGridAdminDto createdDto = new HomepageGridAdminDto();
        createdDto.setId("1");
        createdDto.setTitle("New Grid");

        when(homepageGridAdminService.save(requestDto)).thenReturn(createdDto);

        ResponseEntity<HomepageGridAdminDto> response = homepageGridAdminController.createHomepageGrid(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdDto, response.getBody());
    }

    @Test
    void updateHomepageGrid_ShouldReturnUpdatedGrid() {
        String id = "1";
        HomepageGridAdminDto requestDto = new HomepageGridAdminDto();
        requestDto.setTitle("Updated Grid");
        HomepageGridAdminDto updatedDto = new HomepageGridAdminDto();
        updatedDto.setId(id);
        updatedDto.setTitle("Updated Grid");

        when(homepageGridAdminService.update(id, requestDto)).thenReturn(updatedDto);

        ResponseEntity<HomepageGridAdminDto> response = homepageGridAdminController.updateHomepageGrid(id, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedDto, response.getBody());
    }

    @Test
    void patchHomepageGrid_ShouldReturnUpdatedGrid() {
        String id = "1";
        HomepageGridPatchRequest patchRequest = new HomepageGridPatchRequest();
        patchRequest.setTitle("Patched Grid");
        HomepageGridAdminDto updatedDto = new HomepageGridAdminDto();
        updatedDto.setId(id);
        updatedDto.setTitle("Patched Grid");

        when(homepageGridAdminService.patch(id, patchRequest)).thenReturn(updatedDto);

        ResponseEntity<HomepageGridAdminDto> response = homepageGridAdminController.patchHomepageGrid(id, patchRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedDto, response.getBody());
    }

    @Test
    void patchHomepageGrid_ShouldThrowNotFound_WhenNotFound() {
        String id = "1";
        HomepageGridPatchRequest patchRequest = new HomepageGridPatchRequest();
        patchRequest.setTitle("Patched Grid");

        when(homepageGridAdminService.patch(id, patchRequest)).thenThrow(new HomepageGridException.NotFound("HomepageGrid not found"));

        assertThrows(HomepageGridException.NotFound.class, () -> homepageGridAdminController.patchHomepageGrid(id, patchRequest));
    }

    @Test
    void deleteHomepageGrid_ShouldReturnNoContent() {
        String id = "1";

        doNothing().when(homepageGridAdminService).deleteById(id);

        ResponseEntity<Void> response = homepageGridAdminController.deleteHomepageGrid(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(homepageGridAdminService, times(1)).deleteById(id);
    }
}
