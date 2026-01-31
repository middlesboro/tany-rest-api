package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.mapper.ISkladMapper;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderAdminServiceISkladTest {

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
    private ISkladService iskladService;
    @Mock
    private ISkladProperties iskladProperties;
    @Mock
    private ISkladMapper iskladMapper;

    @InjectMocks
    private OrderAdminServiceImpl orderAdminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_shouldCallISkladService_whenEnabled_and_OrderIsNew() {
        // Arrange
        OrderDto dto = new OrderDto();
        dto.setId(null); // New Order
        dto.setItems(new ArrayList<>());

        Order entity = new Order();
        entity.setStatusHistory(new ArrayList<>());

        Order savedEntity = new Order();
        savedEntity.setOrderIdentifier(123L);
        savedEntity.setStatusHistory(new ArrayList<>());

        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);
        when(orderMapper.toEntity(any(OrderDto.class))).thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(savedEntity);
        when(orderMapper.toDto(savedEntity)).thenReturn(dto);

        when(iskladProperties.isEnabled()).thenReturn(true);
        when(iskladMapper.toCreateNewOrderRequest(dto)).thenReturn(sk.tany.rest.api.dto.isklad.CreateNewOrderRequest.builder().build());

        // Act
        orderAdminService.save(dto);

        // Assert
        verify(iskladService, times(1)).createNewOrder(any());
    }

    @Test
    void save_shouldNotCallISkladService_whenDisabled() {
        // Arrange
        OrderDto dto = new OrderDto();
        dto.setId(null); // New Order
        dto.setItems(new ArrayList<>());

        Order entity = new Order();
        entity.setStatusHistory(new ArrayList<>());

        Order savedEntity = new Order();
        savedEntity.setOrderIdentifier(123L);
        savedEntity.setStatusHistory(new ArrayList<>());

        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);
        when(orderMapper.toEntity(any(OrderDto.class))).thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(savedEntity);
        when(orderMapper.toDto(savedEntity)).thenReturn(dto);

        when(iskladProperties.isEnabled()).thenReturn(false);

        // Act
        orderAdminService.save(dto);

        // Assert
        verify(iskladService, never()).createNewOrder(any());
    }
}
