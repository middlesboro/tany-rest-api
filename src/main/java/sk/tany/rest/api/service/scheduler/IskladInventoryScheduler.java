package sk.tany.rest.api.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.dto.isklad.ISkladResponse;
import sk.tany.rest.api.dto.isklad.InventoryDetailRequest;
import sk.tany.rest.api.dto.isklad.InventoryDetailResult;
import sk.tany.rest.api.service.OneDriveService;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IskladInventoryScheduler {

    private final ISkladService iskladService;
    private final OneDriveService oneDriveService;

    @Scheduled(cron = "0 30 23 * * *")
    public void exportInventoryToOneDrive() {
        log.info("Starting ISklad inventory export to OneDrive");

        InventoryDetailRequest request = InventoryDetailRequest.builder()
                .itemIdList(new ArrayList<>()) // All
                .cached(0)
                .onlyOnStock(1)
                .build();

        try {
            ISkladResponse<InventoryDetailResult> iskladResponse = iskladService.getInventory(request);

            if (iskladResponse == null || iskladResponse.getResponse() == null) {
                log.warn("ISklad response is empty or failed");
                return;
            }

            InventoryDetailResult result = iskladResponse.getResponse();
            if (result.getInventoryDetails() == null || result.getInventoryDetails().isEmpty()) {
                log.info("No inventory details found from ISklad");
                return;
            }

            String csvContent = generateCsv(result.getInventoryDetails());

            LocalDate now = LocalDate.now();
            String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
            String month = now.format(DateTimeFormatter.ofPattern("MM"));
            String fileName = now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ".csv";
            String folderPath = "/tany/sklad/" + year + "/" + month;

            oneDriveService.uploadFile(folderPath, fileName, csvContent.getBytes(StandardCharsets.UTF_8));
            log.info("Successfully exported ISklad inventory to OneDrive: {}/{}", folderPath, fileName);

        } catch (Exception e) {
            log.error("Failed to export ISklad inventory to OneDrive", e);
        }
    }

    private String generateCsv(Map<String, InventoryDetailResult.InventoryDetailItem> inventoryDetails) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,name,ean,count\n");

        for (InventoryDetailResult.InventoryDetailItem item : inventoryDetails.values()) {
            csv.append(escapeCsv(item.getId())).append(",");
            csv.append(escapeCsv(item.getName())).append(",");
            csv.append(escapeCsv(item.getEan())).append(",");

            Integer count = 0;
            if (item.getCountTypes() != null && item.getCountTypes().getAll() != null) {
                count = item.getCountTypes().getAll();
            }
            csv.append(count).append("\n");
        }
        return csv.toString();
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}
