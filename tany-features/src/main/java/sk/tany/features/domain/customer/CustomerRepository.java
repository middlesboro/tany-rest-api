package sk.tany.features.domain.customer;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    public Optional<Customer> findByEmail(String email) ;

    public java.util.List<Customer> findAllByRole(Role role) ;
}
