package sk.tany.rest.api.domain.payment;

import org.junit.jupiter.api.Test;
import sk.tany.rest.api.dto.PaymentDto;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentOrderTest {

    @Test
    void paymentDomainHasOrderField() {
        Payment payment = new Payment();
        payment.setOrder(1);
        assertThat(payment.getOrder()).isEqualTo(1);
    }

    @Test
    void paymentDtoHasOrderField() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setOrder(2);
        assertThat(paymentDto.getOrder()).isEqualTo(2);
    }

}
