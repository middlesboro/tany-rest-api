package sk.tany.rest.api.domain.cartdiscount;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CartDiscountRepository extends MongoRepository<CartDiscount, String> {
    public Optional<CartDiscount> findByCode(String code) ;

    public boolean existsByCode(String code) ;

    public Optional<CartDiscount> findByCodeAndActiveTrue(String code) ;

    public List<CartDiscount> findAllByCodeIsNullAndActiveTrue() ;

    public List<CartDiscount> findAllByAutomaticTrueAndActiveTrue() ;

    public List<CartDiscount> findApplicableAutomaticDiscounts(Set<String> productIds, Set<String> categoryIds, Set<String> brandIds) ;
}
