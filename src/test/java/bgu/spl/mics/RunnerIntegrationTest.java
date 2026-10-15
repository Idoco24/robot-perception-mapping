package bgu.spl.mics;

import bgu.spl.mics.application.GurionRockRunner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RunnerIntegrationTest {
    @TempDir Path workDirectory;

    private String run(String configuration) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        String classpath = Path.of(GurionRockRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(Gson.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path log = workDirectory.resolve("simulation.log");
        Process process = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-cp", classpath, GurionRockRunner.class.getName(),
                Path.of(configuration).toAbsolutePath().toString())
                .directory(workDirectory.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Simulation did not shut down within 20 seconds");
            String output = Files.readString(log);
            assertEquals(0, process.exitValue(), output);
            assertFalse(output.contains("Exception"), output);
            return output;
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor();
            }
        }
    }

    @Test
    void normalScenarioLoadsRelativeInputsAndProducesMapFromAnotherDirectory() throws Exception {
        run("examples/normal/configuration.json");
        JsonObject output = new Gson().fromJson(Files.readString(workDirectory.resolve("out.json")), JsonObject.class);
        assertEquals(1, output.get("numLandmarks").getAsInt());
        assertEquals(2, output.getAsJsonObject("landMarks").getAsJsonObject("wall")
                .getAsJsonArray("coordinates").size());
        assertFalse(Files.exists(workDirectory.resolve("error_output.json")));
    }

    @Test
    void suppliedSensorFailureScenarioProducesDiagnosticReportAndStops() throws Exception {
        run("configuration_file.json");
        JsonObject output = new Gson().fromJson(Files.readString(workDirectory.resolve("error_output.json")), JsonObject.class);
        assertEquals("camera1", output.get("faultySensor").getAsString());
        assertTrue(output.has("statistics"));
        assertTrue(output.has("lastCamerasFrame"));
        assertFalse(Files.exists(workDirectory.resolve("out.json")));
    }
}
