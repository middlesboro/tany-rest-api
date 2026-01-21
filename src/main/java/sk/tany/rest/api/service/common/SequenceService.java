package sk.tany.rest.api.service.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.common.Sequence;
import sk.tany.rest.api.domain.common.SequenceRepository;

@Service
@RequiredArgsConstructor
public class SequenceService {

    private final SequenceRepository sequenceRepository;

    public synchronized long getNextSequence(String seqName) {
        Sequence sequence = sequenceRepository.findById(seqName).orElseGet(() -> {
            Sequence newSeq = new Sequence();
            newSeq.setId(seqName);
            newSeq.setSeq(0L);
            return newSeq;
        });

        long nextVal = sequence.getSeq() + 1;
        sequence.setSeq(nextVal);
        sequenceRepository.save(sequence);
        return nextVal;
    }
}
