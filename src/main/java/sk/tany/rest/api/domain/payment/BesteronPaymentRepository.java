package sk.tany.rest.api.domain.payment;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface BesteronPaymentRepository extends MongoRepository<BesteronPayment, String> {
    Optional<BesteronPayment> findByTransactionId(String transactionId);

    Optional<BesteronPayment> findTopByOrderIdOrderByCreateDateDesc(String orderId);
}
