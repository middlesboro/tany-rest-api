package sk.tany.rest.api.domain.payment;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BesteronPaymentRepository extends MongoRepository<BesteronPayment, String> {
    Optional<BesteronPayment> findTopByOrderIdOrderByCreateDateDesc(String orderId);
    Optional<BesteronPayment> findByTransactionId(String transactionId);
}
