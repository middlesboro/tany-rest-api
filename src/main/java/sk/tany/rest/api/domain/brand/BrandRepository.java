package sk.tany.rest.api.domain.brand;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface BrandRepository extends MongoRepository<Brand, String> {
    public Optional<Brand> findByPrestashopId(Long prestashopId) ;

    public Optional<Brand> findByName(String name) ;
}
