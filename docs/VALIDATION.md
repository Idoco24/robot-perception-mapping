# Validation and review

Prepared on 2026-10-15 with Temurin JDK 21.0.11 and Maven 3.9.9 on Windows 11.

## Recovered baseline

- Copied all 48 Java source/test files and compared their bytes with the original
  folder before editing: no differences.
- Left the original coursework directories and submission archive untouched.
- Original eight tests passed on JDK 21.
- Executed the supplied scenario in a separate JVM: it exited successfully and
  produced the expected camera1 sensor-failure report.

## Corrections with regression evidence

| Problem | Reproduction and correction |
| --- | --- |
| Zero/sub-millisecond future timeout waits forever | Both bounded regression tests failed before the fix. Nanosecond timed waits now use a remaining-time loop and return immediately for non-positive timeouts. |
| Multiple landmarks emitted for one multi-point object | Two-point regression expected one landmark but obtained two. Construction now happens once after transforming all points. |
| Coordinate update changes the last point repeatedly | Regression expected the first averaged x-coordinate to be 1 but obtained 0. Update now uses the matching point index. |
| Inconsistent Java 8/9 compiler target despite Java 21 APIs | Replaced conflicting targets with release 21 and pinned Maven plugin versions. |
| Input data depends on launch working directory | Resolve sensor paths relative to the config. Both process tests launch from fresh temporary directories. |
| Message-bus test subscriptions leak between tests | Added after-test unregistration without changing application state APIs. |

The unbounded `Future.get()` also defers restoring the interruption flag until
resolution, avoiding a repeated interrupt/wait loop that would prevent a resolver
from acquiring the monitor. `isDone()` now observes state under the same monitor.

## Current verification

`mvn verify` compiles and packages the project and executes 17 tests:

- 3 camera tests.
- 2 original fusion tests.
- 3 future timeout/resolution tests.
- 2 multi-point mapping geometry tests.
- 3 message-bus tests.
- 2 runner argument tests.
- 2 full-process tests: normal map output and sensor-failure diagnostic output.

Process checks verify termination within 20 seconds, zero exit status, absence of
logged exceptions, JSON report structure, and the expected landmark/faulty sensor.
They do not verify every possible interleaving, all map coordinates in the
recovered dataset, or the formal course grading requirements.

## Publication review

Only source, build files, documentation and simulation JSON data are intended
for Git. IDE metadata, local build output, cached dependencies, saved submission
archives and generated runtime reports are excluded.

Pattern checks looked for common credential forms, private-key headers, known
student identifiers and absolute user paths. Two local-path comments were removed
before the initial commit. No matching credentials were found in the intended
source/data set. Pattern checks are not a guarantee that every possible secret
format has been detected. No real contact or financial databases were included.

Git commits use the account's GitHub noreply address rather than a personal email.
The first commit documents recovery honestly and does not claim to be an original
January 2025 development commit. No teammate identity or license was invented.

## Remaining limitations

- The runner starts services without a readiness barrier. Initial tick handling
  and the association of the latest pose with queued observations can depend on
  scheduling. This recovery did not redesign the scheduler or guarantee
  deterministic fusion results.
- Singleton components retain state within a JVM. Use a fresh Java process per run.
- Empty/malformed sensor data and LiDAR error paths have incomplete validation;
  some failures are caught/logged rather than propagated to the process exit code.
- Shutdown/cancellation and message-bus unregister/send races need broader
  concurrency testing before reuse outside this simulation.
- The framework retains unchecked generic casts from the recovered implementation.
- Sensor input is prerecorded simulation data; there is no live hardware bridge,
  visualizer or deployable network service.
- Attribution and permission to publish the original coursework still need human
  verification. Keep this repository private in the meantime.
