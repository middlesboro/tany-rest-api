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
public class BesteronTransactionResponse {

    private Transaction transaction;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Transaction {
        private String transactionId;
        private String createdAt;
        private String variableSymbol;
        private String specificSymbol;
        private String updatedAt;
        private String status;
        private String selectedPaymentMethod;
        private String recurringTransactionId;
        private PaymentIntent paymentIntent;
        private List<String> paymentMethods;
        private List<AdditionalParam> additionalParams;
        private List<Item> items;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentIntent {
        private String orderNumber;
        private long totalAmount;
        private String description;
        private String onBehalfOf;
        private int validityTime;
        private String currencyCode;
        private String language;
        private Callback callback;
        private Buyer buyer;
    }

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
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private Address delivery;
        private Address billing;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Address {
        private String postalCode;
        private String city;
        private String countrySubdivisionCode;
        private String countryCode;
        private Recipient recipient;
        private List<String> streetLines;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Recipient {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdditionalParam {
        private String name;
        private String value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String name;
        private long amount;
        private int count;
        private int vatRate;
        private String ean;
        private String productUrl;
        private String type;
    }
}
