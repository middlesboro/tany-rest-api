package sk.tany.rest.api.domain.payment;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

@Repository
public class GlobalPaymentsPaymentRepository extends AbstractInMemoryRepository<GlobalPaymentsPayment> {

    public GlobalPaymentsPaymentRepository(Nitrite nitrite) {
        super(nitrite, GlobalPaymentsPayment.class);
    }
}
