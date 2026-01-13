package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.dto.BlogDto;

import java.util.Optional;

public interface BlogAdminService {
    Page<BlogDto> findAll(Pageable pageable);
    Optional<BlogDto> findById(String id);
    BlogDto save(BlogDto blogDto);
    BlogDto update(String id, BlogDto blogDto);
    void deleteById(String id);
    BlogDto uploadImage(String id, MultipartFile file);
    void deleteImage(String id);
}
