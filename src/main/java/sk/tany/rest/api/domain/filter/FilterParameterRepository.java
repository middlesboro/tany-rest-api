package sk.tany.rest.api.domain.filter;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FilterParameterRepository extends AbstractInMemoryRepository<FilterParameter> {

    public FilterParameterRepository(Nitrite nitrite) {
        super(nitrite, FilterParameter.class);
    }

    public Optional<FilterParameter> findByName(String name) {
        return memoryCache.values().stream()
                .filter(fp -> fp.getName() != null && fp.getName().equals(name))
                .findFirst();
    }

    public List<FilterParameter> findAllByFilterParameterValueIdsContaining(String valueId) {
        return memoryCache.values().stream()
                .filter(fp -> fp.getFilterParameterValueIds() != null && fp.getFilterParameterValueIds().contains(valueId))
                .collect(Collectors.toList());
    }
}
