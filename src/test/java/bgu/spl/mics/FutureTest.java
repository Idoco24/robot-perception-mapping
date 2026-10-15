package bgu.spl.mics;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FutureTest {
    @Test
    void zeroTimeoutDoesNotWaitForResolution() {
        Future<String> future = new Future<>();
        assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> assertNull(future.get(0, TimeUnit.MILLISECONDS)));
    }

    @Test
    void subMillisecondTimeoutDoesNotBecomeAnInfiniteWait() {
        Future<String> future = new Future<>();
        assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> assertNull(future.get(1, TimeUnit.NANOSECONDS)));
    }

    @Test
    void firstResolutionWinsAndIsAvailableWithoutWaiting() {
        Future<String> future = new Future<>();
        future.resolve("first");
        future.resolve("second");
        assertTrue(future.isDone());
        assertEquals("first", future.get());
        assertEquals("first", future.get(0, TimeUnit.SECONDS));
    }
}
