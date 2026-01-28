package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.sequence.update.SequenceUpdateRequest;
import sk.tany.rest.api.service.common.SequenceService;

@RestController
@RequestMapping("/api/admin/sequences")
@Tag(name = "Admin Sequences", description = "Endpoints for managing sequences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SequenceAdminController {

    private final SequenceService sequenceService;

    @PutMapping("/{seqName}")
    @Operation(summary = "Set sequence value")
    public void setSequenceValue(@PathVariable String seqName, @RequestBody SequenceUpdateRequest request) {
        sequenceService.setSequence(seqName, request.getValue());
    }
}
