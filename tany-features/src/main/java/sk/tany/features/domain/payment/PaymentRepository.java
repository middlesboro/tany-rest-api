package sk.tany.features.domain.payment;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {}
