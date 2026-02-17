package sk.tany.rest.api.service.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.dto.isklad.ISkladResponse;
import sk.tany.rest.api.dto.isklad.InventoryDetailResult;
import sk.tany.rest.api.service.OneDriveService;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IskladInventorySchedulerTest {

    @Mock
    private ISkladService iskladService;

    @Mock
    private OneDriveService oneDriveService;

    @InjectMocks
    private IskladInventoryScheduler scheduler;

    @Test
    void exportInventoryToOneDrive_NoResponse_DoesNothing() {
        when(iskladService.getInventory(any())).thenReturn(null);

        scheduler.exportInventoryToOneDrive();

        verify(oneDriveService, never()).uploadFile(any(), any(), any());
    }

    @Test
    void exportInventoryToOneDrive_Success_UploadsCsv() {
        ISkladResponse<InventoryDetailResult> response = new ISkladResponse<>();
        InventoryDetailResult result = new InventoryDetailResult();
        Map<String, InventoryDetailResult.InventoryDetailItem> items = new HashMap<>();

        InventoryDetailResult.InventoryDetailItem item1 = new InventoryDetailResult.InventoryDetailItem();
        item1.setId(1L);
        item1.setName("Test Product");
        item1.setEan("123456");
        InventoryDetailResult.CountTypes countTypes1 = new InventoryDetailResult.CountTypes();
        countTypes1.setAll(10);
        item1.setCountTypes(countTypes1);
        items.put("1", item1);

        InventoryDetailResult.InventoryDetailItem item2 = new InventoryDetailResult.InventoryDetailItem();
        item2.setId(2L);
        item2.setName("Test Product, with comma");
        item2.setEan("654321");
        // CountTypes null check
        item2.setCountTypes(null);
        items.put("2", item2);

        result.setInventoryDetails(items);
        response.setResponse(result);

        when(iskladService.getInventory(any())).thenReturn(response);

        scheduler.exportInventoryToOneDrive();

        ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(oneDriveService).uploadFile(any(), any(), contentCaptor.capture());

        String content = new String(contentCaptor.getValue());
        assertTrue(content.contains("id,name,ean,count"));
        // Since map iteration order is not guaranteed, check containment
        assertTrue(content.contains("1,Test Product,123456,10"));
        assertTrue(content.contains("2,\"Test Product, with comma\",654321,0"));
    }
}
