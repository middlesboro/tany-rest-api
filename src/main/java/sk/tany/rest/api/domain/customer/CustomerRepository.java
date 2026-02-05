package sk.tany.rest.api.domain.customer;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class CustomerRepository extends AbstractInMemoryRepository<Customer> {

    public CustomerRepository(Nitrite nitrite) {
        super(nitrite, Customer.class);
    }

    public Optional<Customer> findByEmail(String email) {
        return memoryCache.values().stream()
                .filter(c -> c.getEmail() != null && c.getEmail().equals(email))
                .findFirst();
    }

    public java.util.List<Customer> findAllByRole(Role role) {
        return memoryCache.values().stream()
                .filter(c -> c.getRole() == role)
                .collect(java.util.stream.Collectors.toList());
    }
}
