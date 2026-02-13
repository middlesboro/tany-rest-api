package sk.tany.rest.api.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Order> findAll(Long orderIdentifier, OrderStatus status, BigDecimal priceFrom, BigDecimal priceTo, String carrierId, String paymentId, Instant createDateFrom, Instant createDateTo, Pageable pageable) {
        Query query = new Query();

        if (orderIdentifier != null) {
            query.addCriteria(Criteria.where("orderIdentifier").is(orderIdentifier));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        if (priceFrom != null) {
            query.addCriteria(Criteria.where("finalPrice").gte(priceFrom));
        }
        if (priceTo != null) {
            query.addCriteria(Criteria.where("finalPrice").lte(priceTo));
        }
        if (carrierId != null) {
            query.addCriteria(Criteria.where("carrierId").is(carrierId));
        }
        if (paymentId != null) {
            query.addCriteria(Criteria.where("paymentId").is(paymentId));
        }
        if (createDateFrom != null) {
            query.addCriteria(Criteria.where("createDate").gte(createDateFrom));
        }
        if (createDateTo != null) {
            query.addCriteria(Criteria.where("createDate").lte(createDateTo));
        }

        long count = mongoTemplate.count(query, Order.class);
        query.with(pageable);
        List<Order> orders = mongoTemplate.find(query, Order.class);

        return new PageImpl<>(orders, pageable, count);
    }
}
