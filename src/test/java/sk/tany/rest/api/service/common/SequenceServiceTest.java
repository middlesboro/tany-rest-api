package sk.tany.rest.api.service.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.common.Sequence;
import sk.tany.rest.api.domain.common.SequenceRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequenceServiceTest {

    @Mock
    private SequenceRepository sequenceRepository;

    @InjectMocks
    private SequenceService sequenceService;

    @Test
    void getNextSequence_shouldIncrementAndReturnNextValue_whenExists() {
        String seqName = "testSeq";
        Sequence sequence = new Sequence();
        sequence.setId(seqName);
        sequence.setSeq(10L);
        when(sequenceRepository.findById(seqName)).thenReturn(Optional.of(sequence));
        when(sequenceRepository.save(any(Sequence.class))).thenAnswer(i -> i.getArgument(0));

        long nextVal = sequenceService.getNextSequence(seqName);

        assertThat(nextVal).isEqualTo(11L);
        assertThat(sequence.getSeq()).isEqualTo(11L);
        verify(sequenceRepository).save(sequence);
    }

    @Test
    void getNextSequence_shouldInitializeAndReturnOne_whenNotExists() {
        String seqName = "testSeq";
        when(sequenceRepository.findById(seqName)).thenReturn(Optional.empty());
        when(sequenceRepository.save(any(Sequence.class))).thenAnswer(i -> i.getArgument(0));

        long nextVal = sequenceService.getNextSequence(seqName);

        assertThat(nextVal).isEqualTo(1L);
        verify(sequenceRepository).save(any(Sequence.class));
    }

    @Test
    void setSequence_shouldUpdateValue() {
        String seqName = "testSeq";
        long newValue = 100L;
        Sequence sequence = new Sequence();
        sequence.setId(seqName);
        sequence.setSeq(10L);
        when(sequenceRepository.findById(seqName)).thenReturn(Optional.of(sequence));
        when(sequenceRepository.save(any(Sequence.class))).thenAnswer(i -> i.getArgument(0));

        sequenceService.setSequence(seqName, newValue);

        assertThat(sequence.getSeq()).isEqualTo(newValue);
        verify(sequenceRepository).save(sequence);
    }

    @Test
    void ensureSequenceAtLeast_shouldUpdate_whenValueIsHigher() {
        String seqName = "testSeq";
        long currentValue = 10L;
        long newValue = 20L;
        Sequence sequence = new Sequence();
        sequence.setId(seqName);
        sequence.setSeq(currentValue);
        when(sequenceRepository.findById(seqName)).thenReturn(Optional.of(sequence));
        when(sequenceRepository.save(any(Sequence.class))).thenAnswer(i -> i.getArgument(0));

        sequenceService.ensureSequenceAtLeast(seqName, newValue);

        assertThat(sequence.getSeq()).isEqualTo(newValue);
        verify(sequenceRepository).save(sequence);
    }

    @Test
    void ensureSequenceAtLeast_shouldNotUpdate_whenValueIsLower() {
        String seqName = "testSeq";
        long currentValue = 30L;
        long newValue = 20L;
        Sequence sequence = new Sequence();
        sequence.setId(seqName);
        sequence.setSeq(currentValue);
        when(sequenceRepository.findById(seqName)).thenReturn(Optional.of(sequence));

        sequenceService.ensureSequenceAtLeast(seqName, newValue);

        assertThat(sequence.getSeq()).isEqualTo(currentValue);
        verify(sequenceRepository, never()).save(any(Sequence.class));
    }
}
