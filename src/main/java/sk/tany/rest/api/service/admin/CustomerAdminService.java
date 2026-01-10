package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CustomerDto;

import java.util.Optional;

public interface CustomerAdminService {
    Page<CustomerDto> findAll(Pageable pageable);
    Optional<CustomerDto> findById(String id);
    CustomerDto save(CustomerDto customerDto);
    CustomerDto patch(String id, sk.tany.rest.api.dto.admin.customer.patch.CustomerPatchRequest patchDto);
    void deleteById(String id);
}
