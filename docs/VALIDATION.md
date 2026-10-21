# Validation

Run `mvn --batch-mode --no-transfer-progress clean verify` with JDK 21 and Maven 3.9+.

## Coverage

The 17 tests cover camera state, fusion, bounded future waits, message delivery,
coordinate transforms, landmark averaging, CLI arguments, and full-process normal
and sensor-failure scenarios. Process checks use fresh temporary directories and
verify termination, exit status, and JSON output structure.

## Limits

- Service startup lacks a readiness barrier; tick and pose ordering can depend on scheduling.
- Singleton state requires a fresh JVM for each simulation.
- Malformed sensor data and LiDAR error paths have partial validation.
- Cancellation and unregister/send races need broader concurrency testing.
- The message framework contains unchecked generic casts.
- Sensor data is prerecorded; there is no live hardware integration.

Generated reports and build output are ignored by Git.
