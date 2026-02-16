package sk.tany.rest.api.domain.payment;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface GlobalPaymentsPaymentRepository extends MongoRepository<GlobalPaymentsPayment, String> {}
