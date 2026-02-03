package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.admin.InventoryDifferenceDto;
import sk.tany.rest.api.dto.isklad.InventoryDetailResult;
import sk.tany.rest.api.dto.isklad.ISkladResponse;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IskladAdminServiceImplTest {

    @Mock
    private ISkladService iskladService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private IskladAdminServiceImpl service;

    private Product productEan;
    private Product productTitle;
    private Product productNoMatch;

    @BeforeEach
    void setUp() {
        productEan = new Product();
        productEan.setId("p1");
        productEan.setTitle("Product EAN");
        productEan.setEan("123456");
        productEan.setQuantity(10);
        productEan.setActive(true);

        productTitle = new Product();
        productTitle.setId("p2");
        productTitle.setTitle("Product Title");
        productTitle.setEan(""); // Empty EAN
        productTitle.setQuantity(20);
        productTitle.setActive(true);

        productNoMatch = new Product();
        productNoMatch.setId("p3");
        productNoMatch.setTitle("No Match");
        productNoMatch.setQuantity(5);
        productNoMatch.setActive(true);
    }

    @Test
    void getInventoryDifferences_shouldMatchByEan() {
        // Mock DB
        when(productRepository.findAll()).thenReturn(List.of(productEan));
        when(orderRepository.findAllByIskladImportDateIsNullAndStatusNot(OrderStatus.CANCELED))
                .thenReturn(Collections.emptyList());

        // Mock iSklad
        InventoryDetailResult.InventoryDetailItem item = new InventoryDetailResult.InventoryDetailItem();
        item.setEan("123456");
        item.setName("Wrong Name");
        InventoryDetailResult.CountTypes ct = new InventoryDetailResult.CountTypes();
        ct.setAll(15); // Difference: 15 (isklad) vs 10 (db)
        item.setCountTypes(ct);

        InventoryDetailResult result = new InventoryDetailResult();
        result.setInventoryDetails(Map.of("k1", item));
        ISkladResponse<InventoryDetailResult> response = new ISkladResponse<>();
        response.setResponse(result);

        when(iskladService.getInventory(any())).thenReturn(response);

        // Execute
        List<InventoryDifferenceDto> diffs = service.getInventoryDifferences();

        // Verify
        assertEquals(1, diffs.size());
        assertEquals("Product EAN", diffs.get(0).getProductName());
        assertEquals(10, diffs.get(0).getDbQuantity());
        assertEquals(15, diffs.get(0).getIskladQuantity());
    }

    @Test
    void getInventoryDifferences_shouldMatchByTitle_whenEanMissing() {
        // Mock DB
        when(productRepository.findAll()).thenReturn(List.of(productTitle));
        when(orderRepository.findAllByIskladImportDateIsNullAndStatusNot(OrderStatus.CANCELED))
                .thenReturn(Collections.emptyList());

        // Mock iSklad
        InventoryDetailResult.InventoryDetailItem item = new InventoryDetailResult.InventoryDetailItem();
        item.setEan("999999");
        item.setName("Product Title");
        InventoryDetailResult.CountTypes ct = new InventoryDetailResult.CountTypes();
        ct.setAll(25); // Difference: 25 vs 20
        item.setCountTypes(ct);

        InventoryDetailResult result = new InventoryDetailResult();
        result.setInventoryDetails(Map.of("k2", item));
        ISkladResponse<InventoryDetailResult> response = new ISkladResponse<>();
        response.setResponse(result);

        when(iskladService.getInventory(any())).thenReturn(response);

        // Execute
        List<InventoryDifferenceDto> diffs = service.getInventoryDifferences();

        // Verify
        assertEquals(1, diffs.size());
        assertEquals("Product Title", diffs.get(0).getProductName());
        assertEquals(20, diffs.get(0).getDbQuantity());
        assertEquals(25, diffs.get(0).getIskladQuantity());
    }

    @Test
    void getInventoryDifferences_shouldDeductPendingOrders() {
        // Mock DB
        when(productRepository.findAll()).thenReturn(List.of(productEan));

        Order order = new Order();
        OrderItem orderItem = new OrderItem();
        orderItem.setId("p1"); // Matches productEan
        orderItem.setQuantity(2);
        order.setItems(List.of(orderItem));

        when(orderRepository.findAllByIskladImportDateIsNullAndStatusNot(OrderStatus.CANCELED))
                .thenReturn(List.of(order));

        // Mock iSklad
        InventoryDetailResult.InventoryDetailItem item = new InventoryDetailResult.InventoryDetailItem();
        item.setEan("123456");
        InventoryDetailResult.CountTypes ct = new InventoryDetailResult.CountTypes();
        ct.setAll(12);
        // iSklad has 12.
        // Pending is 2.
        // Effective iSklad = 12 - 2 = 10.
        // DB has 10.
        // Difference = 0. Should not be reported.
        item.setCountTypes(ct);

        InventoryDetailResult result = new InventoryDetailResult();
        result.setInventoryDetails(Map.of("k1", item));
        ISkladResponse<InventoryDetailResult> response = new ISkladResponse<>();
        response.setResponse(result);

        when(iskladService.getInventory(any())).thenReturn(response);

        // Execute
        List<InventoryDifferenceDto> diffs = service.getInventoryDifferences();

        // Verify
        assertEquals(0, diffs.size());
    }

    @Test
    void getInventoryDifferences_noMatch_shouldCompareWithZero() {
         // Mock DB
        when(productRepository.findAll()).thenReturn(List.of(productNoMatch)); // Qty 5
        when(orderRepository.findAllByIskladImportDateIsNullAndStatusNot(OrderStatus.CANCELED))
                .thenReturn(Collections.emptyList());

        // Mock iSklad - Empty
        InventoryDetailResult result = new InventoryDetailResult();
        result.setInventoryDetails(new HashMap<>());
        ISkladResponse<InventoryDetailResult> response = new ISkladResponse<>();
        response.setResponse(result);

        when(iskladService.getInventory(any())).thenReturn(response);

        // Execute
        List<InventoryDifferenceDto> diffs = service.getInventoryDifferences();

        // Verify
        assertEquals(1, diffs.size());
        assertEquals("No Match", diffs.get(0).getProductName());
        assertEquals(5, diffs.get(0).getDbQuantity());
        assertEquals(0, diffs.get(0).getIskladQuantity());
    }
}
