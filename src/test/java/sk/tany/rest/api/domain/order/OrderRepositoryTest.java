package sk.tany.rest.api.domain.order;

import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryTest {

    @Mock
    private Nitrite nitrite;

    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new OrderRepository(nitrite);
    }

    @Test
    void findAll_shouldFilterByOrderIdentifier() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), Instant.now());
        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        injectOrder(o1);
        injectOrder(o2);

        Page<Order> result = repository.findAll(101L, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(101L, result.getContent().getFirst().getOrderIdentifier());
    }

    @Test
    void findAll_shouldFilterByStatus() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), Instant.now());
        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        injectOrder(o1);
        injectOrder(o2);

        Page<Order> result = repository.findAll(null, OrderStatus.PAID, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(OrderStatus.PAID, result.getContent().getFirst().getStatus());
    }

    @Test
    void findAll_shouldFilterByPriceRange() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), Instant.now());
        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        Order o3 = createOrder("3", 103L, OrderStatus.PAID, new BigDecimal("30.00"), Instant.now());
        injectOrder(o1);
        injectOrder(o2);
        injectOrder(o3);

        Page<Order> result = repository.findAll(null, null, new BigDecimal("15.00"), new BigDecimal("25.00"), null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal("20.00"), result.getContent().getFirst().getFinalPrice());
    }

    @Test
    void findAll_shouldFilterByCarrierId() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), Instant.now());
        o1.setCarrierId("c1");
        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        o2.setCarrierId("c2");
        injectOrder(o1);
        injectOrder(o2);

        Page<Order> result = repository.findAll(null, null, null, null, "c1", null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("c1", result.getContent().getFirst().getCarrierId());
    }

    @Test
    void findAll_shouldFilterByPaymentId() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), Instant.now());
        o1.setPaymentId("p1");
        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        o2.setPaymentId("p2");
        injectOrder(o1);
        injectOrder(o2);

        Page<Order> result = repository.findAll(null, null, null, null, null, "p1", null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("p1", result.getContent().getFirst().getPaymentId());
    }

    @Test
    void findAll_shouldFilterByDateRange() throws Exception {
        Instant now = Instant.now();
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), now.minusSeconds(3600));
        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), now);
        Order o3 = createOrder("3", 103L, OrderStatus.PAID, new BigDecimal("30.00"), now.plusSeconds(3600));
        injectOrder(o1);
        injectOrder(o2);
        injectOrder(o3);

        Page<Order> result = repository.findAll(null, null, null, null, null, null, now.minusSeconds(1800), now.plusSeconds(1800), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("2", result.getContent().getFirst().getId());
    }

    @Test
    void findAll_shouldSortByPrice() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("30.00"), Instant.now());
        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("10.00"), Instant.now());
        Order o3 = createOrder("3", 103L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        injectOrder(o1);
        injectOrder(o2);
        injectOrder(o3);

        Sort sort = Sort.by(Sort.Direction.ASC, "price");
        Page<Order> result = repository.findAll(null, null, null, null, null, null, null, null, PageRequest.of(0, 10, sort));

        assertEquals(3, result.getTotalElements());
        assertEquals(new BigDecimal("10.00"), result.getContent().getFirst().getFinalPrice());
        assertEquals(new BigDecimal("20.00"), result.getContent().get(1).getFinalPrice());
        assertEquals(new BigDecimal("30.00"), result.getContent().get(2).getFinalPrice());
    }

    @Test
    void findAllByCustomerIdAndAuthenticatedUserTrue_shouldFilterCorrectly() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), Instant.now());
        o1.setCustomerId("c1");
        o1.setAuthenticatedUser(true);

        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        o2.setCustomerId("c1");
        o2.setAuthenticatedUser(false);

        Order o3 = createOrder("3", 103L, OrderStatus.PAID, new BigDecimal("30.00"), Instant.now());
        o3.setCustomerId("c2");
        o3.setAuthenticatedUser(true);

        injectOrder(o1);
        injectOrder(o2);
        injectOrder(o3);

        Page<Order> result = repository.findAllByCustomerIdAndAuthenticatedUserTrue("c1", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("1", result.getContent().getFirst().getId());
    }

    @Test
    void findByInvoiceUploadedToOneDriveFalse_shouldReturnCorrectOrders() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), Instant.now());
        o1.setInvoiceUploadedToOneDrive(false);

        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        o2.setInvoiceUploadedToOneDrive(true);

        injectOrder(o1);
        injectOrder(o2);

        var result = repository.findByInvoiceUploadedToOneDriveFalse();

        assertEquals(1, result.size());
        assertEquals("1", result.getFirst().getId());
    }

    @Test
    void findByCreditNoteUploadedToOneDriveFalse_shouldReturnCorrectOrders() throws Exception {
        Order o1 = createOrder("1", 101L, OrderStatus.CREATED, new BigDecimal("10.00"), Instant.now());
        o1.setCreditNoteUploadedToOneDrive(false);

        Order o2 = createOrder("2", 102L, OrderStatus.PAID, new BigDecimal("20.00"), Instant.now());
        o2.setCreditNoteUploadedToOneDrive(true);

        injectOrder(o1);
        injectOrder(o2);

        var result = repository.findByCreditNoteUploadedToOneDriveFalse();

        assertEquals(1, result.size());
        assertEquals("1", result.getFirst().getId());
    }

    private Order createOrder(String id, Long identifier, OrderStatus status, BigDecimal price, Instant createDate) {
        Order order = new Order();
        order.setId(id);
        order.setOrderIdentifier(identifier);
        order.setStatus(status);
        order.setFinalPrice(price);
        order.setCreateDate(createDate);
        return order;
    }

    @SuppressWarnings("unchecked")
    private void injectOrder(Order order) throws Exception {
        Field cacheField = AbstractInMemoryRepository.class.getDeclaredField("memoryCache");
        cacheField.setAccessible(true);
        Map<String, Order> cache = (Map<String, Order>) cacheField.get(repository);
        cache.put(order.getId(), order);
    }
}
