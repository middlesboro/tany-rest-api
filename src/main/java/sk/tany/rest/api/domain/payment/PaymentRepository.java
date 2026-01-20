package sk.tany.rest.api.domain.payment;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

@Repository
public class PaymentRepository extends AbstractInMemoryRepository<Payment> {

    public PaymentRepository(Nitrite nitrite) {
        super(nitrite, Payment.class);
    }
}
