package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.mapper.ISkladMapper;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAdminServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private EmailService emailService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CartDiscountRepository cartDiscountRepository;
    @Mock
    private SequenceService sequenceService;
    @Mock
    private ISkladProperties iskladProperties;
    @Mock
    private ISkladService iskladService;
    @Mock
    private ISkladMapper iskladMapper;

    @InjectMocks
    private OrderAdminServiceImpl orderAdminService;

    @Test
    void createOrder_shouldCalculatePricesAndSave() {
        // Given
        OrderDto inputDto = new OrderDto();
        inputDto.setCarrierId("carrier1");
        inputDto.setPaymentId("payment1");
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setId("prod1");
        itemDto.setQuantity(2);
        inputDto.setItems(List.of(itemDto));

        Product product = new Product();
        product.setId("prod1");
        product.setTitle("Test Product");
        product.setPrice(new BigDecimal("10.00"));
        product.setWeight(new BigDecimal("1.0"));

        Carrier carrier = new Carrier();
        carrier.setId("carrier1");
        carrier.setName("Test Carrier");
        // Carrier logic in service defaults to 0 if ranges empty/no match, assume 0 for simplicity or mock ranges if needed

        Payment payment = new Payment();
        payment.setId("payment1");
        payment.setName("Test Payment");
        payment.setPrice(new BigDecimal("5.00"));

        when(productRepository.findById("prod1")).thenReturn(Optional.of(product));
        when(carrierRepository.findById("carrier1")).thenReturn(Optional.of(carrier));
        when(paymentRepository.findById("payment1")).thenReturn(Optional.of(payment));
        when(sequenceService.getNextSequence("order_identifier")).thenReturn(100L);

        Order savedOrder = new Order();
        savedOrder.setId("newId");
        savedOrder.setOrderIdentifier(100L);
        savedOrder.setFinalPrice(new BigDecimal("25.00")); // 2*10 + 0 + 5 = 25

        when(orderMapper.toEntity(any(OrderDto.class))).thenReturn(savedOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderDto resultDto = new OrderDto();
        resultDto.setId("newId");
        resultDto.setFinalPrice(new BigDecimal("25.00"));
        when(orderMapper.toDto(savedOrder)).thenReturn(resultDto);

        // When
        OrderDto result = orderAdminService.save(inputDto);

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals("newId", result.getId());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("20.00"), inputDto.getProductsPrice()); // 2 * 10
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("25.00"), inputDto.getFinalPrice()); // 20 + 5 + 0

        org.junit.jupiter.api.Assertions.assertNotNull(inputDto.getPriceBreakDown());
        org.junit.jupiter.api.Assertions.assertFalse(inputDto.getPriceBreakDown().getItems().isEmpty());

        // Check Breakdown Totals (Assuming VAT logic sets defaults if missing or simple calc)
        // Since we didn't mock priceWithoutVat/vatValue in inputs, they might be derived or zero depending on implementation detail of "defaults"
        // But we can check that breakdown structure is populated.
        org.junit.jupiter.api.Assertions.assertNotNull(inputDto.getPriceBreakDown().getTotalPrice());
    }

    @Test
    void createOrder_shouldApplyFreeShippingThreshold() {
        // Given
        OrderDto inputDto = new OrderDto();
        inputDto.setCarrierId("carrier1");
        inputDto.setPaymentId("payment1");
        // High value item to trigger free shipping
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setId("prod1");
        itemDto.setQuantity(1);
        inputDto.setItems(List.of(itemDto));

        Product product = new Product();
        product.setId("prod1");
        product.setTitle("Expensive Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setWeight(new BigDecimal("1.0"));

        Carrier carrier = new Carrier();
        carrier.setId("carrier1");
        carrier.setName("Test Carrier");
        sk.tany.rest.api.domain.carrier.CarrierPriceRange range = new sk.tany.rest.api.domain.carrier.CarrierPriceRange();
        range.setWeightFrom(new BigDecimal("0"));
        range.setWeightTo(new BigDecimal("10"));
        range.setPrice(new BigDecimal("10.00"));
        range.setFreeShippingThreshold(new BigDecimal("50.00")); // Threshold met
        carrier.setRanges(List.of(range));

        Payment payment = new Payment();
        payment.setId("payment1");
        payment.setName("Test Payment");
        payment.setPrice(new BigDecimal("0.00"));

        when(productRepository.findById("prod1")).thenReturn(Optional.of(product));
        when(carrierRepository.findById("carrier1")).thenReturn(Optional.of(carrier));
        when(paymentRepository.findById("payment1")).thenReturn(Optional.of(payment));
        when(sequenceService.getNextSequence("order_identifier")).thenReturn(101L);

        Order savedOrder = new Order();
        savedOrder.setId("newId2");
        savedOrder.setOrderIdentifier(101L);
        savedOrder.setFinalPrice(new BigDecimal("100.00")); // 100 + 0 (free ship) + 0

        when(orderMapper.toEntity(any(OrderDto.class))).thenReturn(savedOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderDto resultDto = new OrderDto();
        resultDto.setId("newId2");
        when(orderMapper.toDto(savedOrder)).thenReturn(resultDto);

        // When
        orderAdminService.save(inputDto);

        // Then
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("100.00"), inputDto.getFinalPrice());
    }

    @Test
    void update_shouldSendEmail_whenStatusChangesToSent() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.SENT);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.SENT);
        updatedOrder.setEmail("test@example.com");
        updatedOrder.setFirstname("John");
        updatedOrder.setOrderIdentifier(100L);
        updatedOrder.setCarrierOrderStateLink("http://track.me");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);

        orderAdminService.update(orderId, orderDto);

        verify(emailService, times(1)).sendEmail(eq("test@example.com"), contains("Objednávka odoslaná"), anyString(), eq(true), any());
    }

    @Test
    void update_shouldSendEmail_whenStatusChangesToPaid() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.PAID);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.PAID);
        updatedOrder.setEmail("test@example.com");
        updatedOrder.setFirstname("John");
        updatedOrder.setOrderIdentifier(100L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);

        orderAdminService.update(orderId, orderDto);

        verify(emailService, times(1)).sendEmail(eq("test@example.com"), contains("Objednávka zaplatená"), anyString(), eq(true), any());
    }

    @Test
    void update_shouldNotSendEmail_whenStatusDoesNotChangeToSentOrPaid() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.CANCELED);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.CANCELED);
        updatedOrder.setEmail("test@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);

        orderAdminService.update(orderId, orderDto);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    void update_shouldNotSendEmail_whenStatusWasAlreadySent() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.SENT);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.SENT);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.SENT);
        updatedOrder.setEmail("test@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);

        orderAdminService.update(orderId, orderDto);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    void update_shouldSetCancelDateAndCreditNoteIdentifier_whenStatusChangesToCanceled() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.CANCELED);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);
        when(sequenceService.getNextSequence("credit_note_identifier")).thenReturn(555L);

        orderAdminService.update(orderId, orderDto);

        verify(sequenceService, times(1)).getNextSequence("credit_note_identifier");
        assertNotNull(updatedOrder.getCancelDate());
        assertEquals(555L, updatedOrder.getCreditNoteIdentifier());
    }
}
