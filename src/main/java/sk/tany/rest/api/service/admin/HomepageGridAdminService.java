package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.admin.homepage.HomepageGridAdminDto;
import sk.tany.rest.api.dto.admin.homepage.patch.HomepageGridPatchRequest;

import java.util.Optional;

public interface HomepageGridAdminService {
    Page<HomepageGridAdminDto> findAll(Pageable pageable);
    Optional<HomepageGridAdminDto> findById(String id);
    HomepageGridAdminDto save(HomepageGridAdminDto dto);
    HomepageGridAdminDto update(String id, HomepageGridAdminDto dto);
    HomepageGridAdminDto patch(String id, HomepageGridPatchRequest dto);
    void deleteById(String id);
}
