package sk.tany.rest.api.domain.payment;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Comparator;
import java.util.Optional;

@Repository
public class BesteronPaymentRepository extends AbstractInMemoryRepository<BesteronPayment> {

    public BesteronPaymentRepository(Nitrite nitrite) {
        super(nitrite, BesteronPayment.class);
    }

    public Optional<BesteronPayment> findByTransactionId(String transactionId) {
        return memoryCache.values().stream()
                .filter(bp -> bp.getTransactionId() != null && bp.getTransactionId().equals(transactionId))
                .findFirst();
    }

    public Optional<BesteronPayment> findTopByOrderIdOrderByCreateDateDesc(String orderId) {
        return memoryCache.values().stream()
                .filter(bp -> bp.getOrderId() != null && bp.getOrderId().equals(orderId))
                .sorted(Comparator.comparing(BesteronPayment::getCreatedDate).reversed())
                .findFirst();
    }
}
