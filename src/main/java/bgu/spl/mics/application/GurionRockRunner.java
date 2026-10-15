package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.application.configuration.InputCamera.CameraConfiguration;
import bgu.spl.mics.application.configuration.InputFile;
import bgu.spl.mics.application.configuration.InputLidarWorkers.LidarConfiguration;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

public class GurionRockRunner {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: GurionRockRunner <configuration.json>");
        }
        StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();

        String configFilePath = args[0];
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Enable pretty printing
        try (Reader reader = new FileReader(configFilePath)) {
            InputFile inputFile = gson.fromJson(reader, InputFile.class);
            if (inputFile == null || inputFile.getCameras() == null || inputFile.getLidarWorkers() == null
                    || inputFile.getTickTime() <= 0 || inputFile.getDuration() <= 0) {
                throw new IllegalArgumentException("Configuration requires Cameras, LiDarWorkers, positive TickTime and Duration.");
            }
            Path configDirectory = Path.of(configFilePath).toAbsolutePath().getParent();
            inputFile.getCameras().setCameraDatasPath(configDirectory.resolve(inputFile.getCameras().getCameraDatasPath()).toString());
            inputFile.getLidarWorkers().setLidarsDataPath(configDirectory.resolve(inputFile.getLidarWorkers().getLidarsDataPath()).toString());
            inputFile.setPoseJsonFile(configDirectory.resolve(inputFile.getPoseJsonFile()).toString());
            // Create Cameras
            List<Camera> cameras = new ArrayList<>();
            for (CameraConfiguration config : inputFile.getCameras().getCamerasConfigurations()) {
                List<StampedDetectedObjects> detectedObjectsList = inputFile.getCameras().getDetectedObjectsList(config.getCameraKey());
                Camera camera = new Camera(config.getId(), config.getFrequency(), config.getCameraKey(), detectedObjectsList);
                cameras.add(camera);
            }

            LiDarDataBase dataBase = LiDarDataBase.getInstance(inputFile.getLidarWorkers().getLidarsDataPath());
            // Create LiDar Workers
            List<LiDarWorkerTracker> lidarWorkers = new ArrayList<>();
            for (LidarConfiguration config : inputFile.getLidarWorkers().getLidarConfigurations()) {
                LiDarWorkerTracker lidarWorker = new LiDarWorkerTracker(config.getId(), config.getFrequency(), dataBase);
                lidarWorkers.add(lidarWorker);
            }
            // System.out.println("Number of LiDar Workers: " + lidarWorkers.size());
            // System.out.println("Number of TrackedObjects: " + dataBase.getCloudPoints().size());

            // Create FusionSlam and StatisticalFolder
            FusionSlam fusionSlam = FusionSlam.getInstance();
            // Create GPSIMU
            GPSIMU gpsimu = new GPSIMU(inputFile.getPoseJsonFile());
            // Initialize Services
            List<Thread> threads = new ArrayList<>();
            for (Camera camera : cameras) {
                CameraService cameraService = new CameraService(camera);
                Thread thread = new Thread(cameraService);
                threads.add(thread);
            }
            for (LiDarWorkerTracker lidarWorker : lidarWorkers) {
                LiDarService lidarWorkerService = new LiDarService(lidarWorker);
                Thread thread = new Thread(lidarWorkerService);
                threads.add(thread);
            }
            FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);
            Thread fusionSlamThread = new Thread(fusionSlamService);
            threads.add(fusionSlamThread);
            PoseService poseService = new PoseService(gpsimu);
            Thread poseServiceThread = new Thread(poseService);
            threads.add(poseServiceThread);
            TimeService timeService = new TimeService(inputFile.getTickTime(), inputFile.getDuration());
            Thread timeServiceThread = new Thread(timeService);
            threads.add(timeServiceThread);
            // Start all services
            for (Thread thread : threads) {
                thread.start();
            }
            // Wait for all services to finish
            for (Thread thread : threads) {
                thread.join();
            }

            // Output statistics
            System.out.println("System Runtime: " + statisticalFolder.getSystemRuntime());
            System.out.println("Number of Detected Objects: " + statisticalFolder.getNumDetectedObjects());
            System.out.println("Number of Tracked Objects: " + statisticalFolder.getNumTrackedObjects());
            System.out.println("Number of Landmarks: " + statisticalFolder.getNumLandmarks());

        }
    }
}
