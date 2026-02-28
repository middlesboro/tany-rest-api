package sk.tany.rest.api.service.chat;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderTools {

    private final OrderRepository orderRepository;

    @Tool("Check the status of an order given its ID and email or phone number. " +
            "The orderIdentifier must be a number. " +
            "The emailOrPhone can be an email address or a phone number." +
            "Use slovak text from status mapping " +
            "    CREATED - Objednávka je prijatá a čaká na zabalenie" +
            "    PAID  - Objednávka je prijatá, uhradená a čaká na zabalenie" +
            "    COD  - Objednávka je prijatá a čaká na zabalenie. " +
            "    PACKING - Objednávka sa práve balí a čoskoro bude odoslaná,\n" +
            "    PACKED - Objednávka je zabalená a pripravená na odoslanie,\n" +
            "    SENT - Objednávka bola odoslaná,\n" +
            "    READY_FOR_PICKUP - Objednávka je pripravená na odbernom mieste,\n" +
            "    DELIVERED - Objednávka bola úspešne doručená,\n" +
            "    CANCELED - Objednávka bola zrušená. V prípade otázok nás prosím kontaktujte \n" +
            "If there is a tracking link append 'Balík môžete sledovať na tomto odkaze: ' Translate response to slovak language.")
    public String checkOrderStatus(String orderIdentifier, String emailOrPhone) {
        long id;
        try {
            id = Long.parseLong(orderIdentifier);
        } catch (NumberFormatException e) {
            return "Invalid order identifier format. It must be a number.";
        }

        Optional<Order> orderOpt = orderRepository.findByOrderIdentifier(id);
        if (orderOpt.isEmpty()) {
            return "Order with identifier " + orderIdentifier + " not found.";
        }

        Order order = orderOpt.get();

        String response = "Order status: ";

        // Check email
        if (emailOrPhone.equalsIgnoreCase(order.getEmail()) || checkPhone(order.getPhone(), emailOrPhone)) {
            response = response + order.getStatus();
            if (StringUtils.isNotBlank(order.getCarrierOrderStateLink())) {
                response = response + ". You can track your order here: " + order.getCarrierOrderStateLink();
            }

            return response;
        }

        return "Order found, but the provided email or phone number does not match our records.";
    }

    private boolean checkPhone(String storedPhone, String inputPhone) {
        if (storedPhone == null || inputPhone == null) {
            return false;
        }

        String normalizedStored = normalize(storedPhone);
        String normalizedInput = normalize(inputPhone);

        if (normalizedStored.isEmpty() || normalizedInput.isEmpty()) {
            return false;
        }

        if (normalizedStored.equals(normalizedInput)) {
            return true;
        }

        // Try variations for input phone
        // If input starts with 421 (from +421), try replacing with 0
        if (normalizedInput.startsWith("421")) {
            String variant = "0" + normalizedInput.substring(3);
            if (normalizedStored.equals(variant)) return true;
        }

        // If input starts with 0, try replacing with 421
        if (normalizedInput.startsWith("0")) {
            String variant = "421" + normalizedInput.substring(1);
            if (normalizedStored.equals(variant)) return true;
        }

        // Try variations for stored phone (in case stored is in different format)
        if (normalizedStored.startsWith("421")) {
            String variant = "0" + normalizedStored.substring(3);
            if (normalizedInput.equals(variant)) return true;
        }

        if (normalizedStored.startsWith("0")) {
            String variant = "421" + normalizedStored.substring(1);
            if (normalizedInput.equals(variant)) return true;
        }

        return false;
    }

    private String normalize(String phone) {
        // Remove all non-digit characters
        return phone.replaceAll("[^0-9]", "");
    }
}
