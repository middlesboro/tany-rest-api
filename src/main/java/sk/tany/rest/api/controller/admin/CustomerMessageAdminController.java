package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.domain.customermessage.CustomerMessage;
import sk.tany.rest.api.domain.customermessage.CustomerMessageRepository;
import sk.tany.rest.api.dto.admin.customermessage.CustomerMessageDto;

@RestController
@RequestMapping("/api/admin/customer-messages")
@RequiredArgsConstructor
@Tag(name = "Customer Message Admin Controller")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerMessageAdminController {

    private final CustomerMessageRepository repository;

    @GetMapping
    @Operation(summary = "Get all customer messages")
    public Page<CustomerMessageDto> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer message by id")
    public CustomerMessageDto findById(@PathVariable String id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Customer message not found with id: " + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer message")
    public void delete(@PathVariable String id) {
        repository.deleteById(id);
    }

    private CustomerMessageDto toDto(CustomerMessage entity) {
        return CustomerMessageDto.builder()
                .id(entity.getId())
                .message(entity.getMessage())
                .email(entity.getEmail())
                .createDate(entity.getCreateDate())
                .build();
    }
}
