package sk.tany.rest.api.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String>, OrderRepositoryCustom {

    Optional<Order> findByOrderIdentifier(Long orderIdentifier);

    Optional<Order> findByCartId(String cartId);

    List<Order> findByCartIdIn(java.util.Collection<String> cartIds);

    List<Order> findAllByCustomerId(String customerId);

    Page<Order> findAllByCustomerId(String customerId, Pageable pageable);

    Page<Order> findAllByCustomerIdAndAuthenticatedUserTrue(String customerId, Pageable pageable);

    List<Order> findByInvoiceUploadedToOneDriveFalse();

    List<Order> findByCreditNoteUploadedToOneDriveFalse();

    List<Order> findAllByIskladImportDateIsNullAndStatusNot(OrderStatus status);

    List<Order> findAllByAdminNotificationDateIsNull();
}
