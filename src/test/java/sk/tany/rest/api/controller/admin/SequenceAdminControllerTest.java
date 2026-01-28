package sk.tany.rest.api.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.dto.admin.sequence.update.SequenceUpdateRequest;
import sk.tany.rest.api.service.common.SequenceService;

import static org.mockito.Mockito.verify;

class SequenceAdminControllerTest {

    @Mock
    private SequenceService sequenceService;

    @InjectMocks
    private SequenceAdminController sequenceAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void setSequenceValue_ShouldCallService() {
        String seqName = "order_identifier";
        long value = 5L;
        SequenceUpdateRequest request = new SequenceUpdateRequest();
        request.setValue(value);

        sequenceAdminController.setSequenceValue(seqName, request);

        verify(sequenceService).setSequence(seqName, value);
    }
}
