package sk.tany.rest.api.domain.order;

public enum OrderStatus {
    CREATED,
    PAID,
    COD,
    PACKING,
    PACKED,
    SENT,
    READY_FOR_PICKUP,
    DELIVERED,
    CANCELED
}
