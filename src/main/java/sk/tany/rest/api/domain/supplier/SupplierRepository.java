package sk.tany.rest.api.domain.supplier;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends MongoRepository<Supplier, String> {
    public Optional<Supplier> findByPrestashopId(Long prestashopId) ;

    public Optional<Supplier> findByName(String name) ;
}
