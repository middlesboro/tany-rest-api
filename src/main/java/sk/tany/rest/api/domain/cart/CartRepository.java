package sk.tany.rest.api.domain.cart;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {}
