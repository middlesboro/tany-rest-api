package sk.tany.rest.api.service.common;

import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.tany.rest.api.domain.common.SequenceRepository;

import java.io.File;

public class SequenceServiceReproductionTest {

    private SequenceService sequenceService;
    private SequenceRepository sequenceRepository;
    private Nitrite nitrite;
    private String dbPath = "test_sequence.db";

    @BeforeEach
    public void setUp() {
        // Use file-based nitrite to test persistence if needed, but in-memory is enough to test "update on non-existent"
        // Let's use file to be sure about persistence across "restarts" (simulated by new repo instance)
        new File(dbPath).delete();
        nitrite = Nitrite.builder().compressed().filePath(dbPath).openOrCreate();
        sequenceRepository = new SequenceRepository(nitrite);
        sequenceRepository.init();
        sequenceService = new SequenceService(sequenceRepository);
    }

    @AfterEach
    public void tearDown() {
        if (nitrite != null && !nitrite.isClosed()) {
            nitrite.close();
        }
        new File(dbPath).delete();
    }

    @Test
    public void testSequenceIncrementInSameSession() {
        long val1 = sequenceService.getNextSequence("order_identifier");
        Assertions.assertEquals(1, val1, "First sequence should be 1");

        long val2 = sequenceService.getNextSequence("order_identifier");
        Assertions.assertEquals(2, val2, "Second sequence should be 2");
    }

    @Test
    public void testSequencePersistenceAcrossRestarts() {
        long val1 = sequenceService.getNextSequence("order_identifier");
        Assertions.assertEquals(1, val1, "First sequence should be 1");

        // Close and reopen (simulate restart)
        nitrite.close();

        nitrite = Nitrite.builder().compressed().filePath(dbPath).openOrCreate();
        sequenceRepository = new SequenceRepository(nitrite);
        sequenceRepository.init(); // This should load from DB
        sequenceService = new SequenceService(sequenceRepository);

        long val2 = sequenceService.getNextSequence("order_identifier");
        Assertions.assertEquals(2, val2, "Sequence should persist and be 2 after restart");
    }
}
