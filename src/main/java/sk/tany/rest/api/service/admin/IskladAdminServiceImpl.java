package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.admin.InventoryDifferenceDto;
import sk.tany.rest.api.dto.isklad.InventoryDetailRequest;
import sk.tany.rest.api.dto.isklad.InventoryDetailResult;
import sk.tany.rest.api.dto.isklad.ISkladResponse;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IskladAdminServiceImpl implements IskladAdminService {

    private final ISkladService iskladService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<InventoryDifferenceDto> getInventoryDifferences() {
        // 1. Get Pending Orders (not exported to iSklad and not canceled)
        List<Order> pendingOrders = orderRepository.findAllByIskladImportDateIsNullAndStatusNot(OrderStatus.CANCELED);
        Map<String, Integer> pendingQtyByProductId = new HashMap<>();
        for (Order order : pendingOrders) {
            if (order.getItems() != null) {
                for (var item : order.getItems()) {
                    if (item.getId() != null && item.getQuantity() != null) {
                        pendingQtyByProductId.merge(item.getId(), item.getQuantity(), Integer::sum);
                    }
                }
            }
        }

        // 2. Get iSklad Inventory
        InventoryDetailRequest request = InventoryDetailRequest.builder()
                .itemIdList(new ArrayList<>()) // All
                .cached(0)
                .onlyOnStock(0)
                .build();
        ISkladResponse<InventoryDetailResult> iskladResponse = iskladService.getInventory(request);

        Map<String, InventoryDetailResult.InventoryDetailItem> iskladByEan = new HashMap<>();
        Map<String, InventoryDetailResult.InventoryDetailItem> iskladByName = new HashMap<>();

        if (iskladResponse != null && iskladResponse.getResponse() != null && iskladResponse.getResponse().getInventoryDetails() != null) {
            for (var entry : iskladResponse.getResponse().getInventoryDetails().values()) {
                if (entry.getEan() != null && !entry.getEan().isEmpty()) {
                    iskladByEan.put(entry.getEan(), entry);
                }
                if (entry.getName() != null && !entry.getName().isEmpty()) {
                    iskladByName.put(entry.getName(), entry);
                }
            }
        }

        // 3. Compare with Local Active Products
        List<Product> activeProducts = productRepository.findAll().stream()
                .filter(Product::isActive)
                .toList();

        List<InventoryDifferenceDto> differences = new ArrayList<>();

        for (Product product : activeProducts) {
            Integer dbQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
            Integer pendingQty = pendingQtyByProductId.getOrDefault(product.getId(), 0);

            InventoryDetailResult.InventoryDetailItem matchedItem = null;

            if (product.getEan() != null && !product.getEan().isEmpty()) {
                matchedItem = iskladByEan.get(product.getEan());
            } else {
                matchedItem = iskladByName.get(product.getTitle());
            }

            Integer iskladRaw = 0;
            if (matchedItem != null && matchedItem.getCountTypes() != null) {
                var ct = matchedItem.getCountTypes();
                int all = ct.getAll() != null ? ct.getAll() : 0;
                int expired = ct.getExpired() != null ? ct.getExpired() : 0;
                int damaged = ct.getDamaged() != null ? ct.getDamaged() : 0;
                int ordered = ct.getOrdered() != null ? ct.getOrdered() : 0;
                int reserved = ct.getReserved() != null ? ct.getReserved() : 0;

                // Formula: all - (expired + damaged + ordered + reserved)
                iskladRaw = all - (expired + damaged + ordered + reserved);
            }

            // Deduct pending quantity from iSklad raw quantity because iSklad considers them available (free to order)
            // while we know they are pending export.
            Integer iskladEffective = iskladRaw - pendingQty;

            if (!dbQuantity.equals(iskladEffective)) {
                differences.add(InventoryDifferenceDto.builder()
                        .productName(product.getTitle())
                        .dbQuantity(dbQuantity)
                        .iskladQuantity(iskladEffective)
                        .build());
            }
        }

        return differences;
    }
}
