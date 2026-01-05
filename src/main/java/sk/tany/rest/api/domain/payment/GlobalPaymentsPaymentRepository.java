package sk.tany.rest.api.domain.payment;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalPaymentsPaymentRepository extends MongoRepository<GlobalPaymentsPayment, String> {
    Optional<GlobalPaymentsPayment> findTopByOrderIdOrderByCreateDateDesc(String orderId);
}
