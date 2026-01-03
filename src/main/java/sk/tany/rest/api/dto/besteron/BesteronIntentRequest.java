package sk.tany.rest.api.dto.besteron;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BesteronIntentRequest {

    private int totalAmount;
    private String currencyCode;
    private String orderNumber;
    private String language;
    private List<String> paymentMethods;
    private Callback callback;
    private Buyer buyer;
    private List<Item> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Callback {
        private String returnUrl;
        private String notificationUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Buyer {
        private String email;
        private String firstName;
        private String lastName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String name;
        private String type;
        private int amount;
        private int count;
    }
}
