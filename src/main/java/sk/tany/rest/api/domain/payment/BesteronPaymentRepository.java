package sk.tany.rest.api.domain.payment;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Comparator;
import java.util.Optional;

@Repository
public interface BesteronPaymentRepository extends MongoRepository<BesteronPayment, String> {
    public Optional<BesteronPayment> findByTransactionId(String transactionId) ;

    public Optional<BesteronPayment> findTopByOrderIdOrderByCreateDateDesc(String orderId) ;
}
