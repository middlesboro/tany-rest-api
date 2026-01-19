package sk.tany.rest.api.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Product> findAll(ProductFilter filter, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteriaList = new ArrayList<>();

        if (filter.getQuery() != null && !filter.getQuery().isEmpty()) {
            criteriaList.add(Criteria.where("title").regex(Pattern.compile(Pattern.quote(filter.getQuery()), Pattern.CASE_INSENSITIVE)));
            query.collation(Collation.of("en").strength(Collation.Strength.PRIMARY));
        }

        if (filter.getPriceFrom() != null) {
            criteriaList.add(Criteria.where("price").gte(filter.getPriceFrom()));
        }

        if (filter.getPriceTo() != null) {
            criteriaList.add(Criteria.where("price").lte(filter.getPriceTo()));
        }

        if (filter.getBrandId() != null && !filter.getBrandId().isEmpty()) {
            criteriaList.add(Criteria.where("brandId").is(filter.getBrandId()));
        }

        if (filter.getId() != null && !filter.getId().isEmpty()) {
            criteriaList.add(Criteria.where("id").is(filter.getId()));
        }

        if (filter.getExternalStock() != null) {
            if (filter.getExternalStock()) {
                criteriaList.add(Criteria.where("status").is(ProductStatus.AVAILABLE_ON_EXTERNAL_STOCK));
            } else {
                criteriaList.add(Criteria.where("status").ne(ProductStatus.AVAILABLE_ON_EXTERNAL_STOCK));
            }
        }

        if (filter.getQuantity() != null) {
            criteriaList.add(Criteria.where("quantity").is(filter.getQuantity()));
        }

        if (filter.getActive() != null) {
            criteriaList.add(Criteria.where("active").is(filter.getActive()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long count = mongoTemplate.count(Query.of(query).limit(0).skip(0), Product.class);
        List<Product> products = mongoTemplate.find(query, Product.class);

        return new PageImpl<>(products, pageable, count);
    }
}
