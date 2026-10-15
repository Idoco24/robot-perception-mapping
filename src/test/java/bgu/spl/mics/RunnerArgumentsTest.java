package bgu.spl.mics;

import bgu.spl.mics.application.GurionRockRunner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RunnerArgumentsTest {
    @Test
    void missingConfigurationExplainsUsage() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> GurionRockRunner.main(new String[0]));
        assertTrue(error.getMessage().contains("<configuration.json>"));
    }

    @Test
    void extraArgumentsAreRejectedBeforeStartingServices() {
        assertThrows(IllegalArgumentException.class,
                () -> GurionRockRunner.main(new String[]{"first.json", "second.json"}));
    }
}
