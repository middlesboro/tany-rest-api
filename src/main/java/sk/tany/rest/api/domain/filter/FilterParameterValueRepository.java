package sk.tany.rest.api.domain.filter;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class FilterParameterValueRepository extends AbstractInMemoryRepository<FilterParameterValue> {

    public FilterParameterValueRepository(Nitrite nitrite) {
        super(nitrite, FilterParameterValue.class);
    }

    public Optional<FilterParameterValue> findByNameAndFilterParameterId(String name, String filterParameterId) {
        return memoryCache.values().stream()
                .filter(fpv -> fpv.getName() != null && fpv.getName().equals(name))
                .filter(fpv -> fpv.getFilterParameterId() != null && fpv.getFilterParameterId().equals(filterParameterId))
                .findFirst();
    }
}
