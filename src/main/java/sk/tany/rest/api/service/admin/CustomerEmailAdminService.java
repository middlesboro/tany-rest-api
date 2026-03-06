package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.admin.customeremail.CustomerEmailDto;
import sk.tany.rest.api.dto.admin.customeremail.patch.CustomerEmailPatchRequest;

import java.util.Optional;

public interface CustomerEmailAdminService {
    CustomerEmailDto save(CustomerEmailDto dto);
    Page<CustomerEmailDto> findAll(Pageable pageable);
    Optional<CustomerEmailDto> findById(String id);
    CustomerEmailDto update(String id, CustomerEmailDto dto);
    CustomerEmailDto patch(String id, CustomerEmailPatchRequest patch);
    void deleteById(String id);
}
