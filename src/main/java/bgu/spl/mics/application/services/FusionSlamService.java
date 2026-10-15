package bgu.spl.mics.application.services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.ErrorObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;

public class FusionSlamService extends MicroService {

    private final FusionSlam fusionSlam;
    private Pose currentPose;
    private ErrorObject errorObject = ErrorObject.getInstance();
    private final StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();
    private final ConcurrentMap<String, Boolean> microServiceStatus = new ConcurrentHashMap<>();

    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
    }

    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            System.out.println(getName() + " received TickBroadcast: " + c.getCurrentTick());
        });

        subscribeEvent(PoseEvent.class, (PoseEvent event) -> {
            System.out.println(getName() + " received PoseEvent.");
            currentPose = event.getPose();
        });

        subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent event) -> {
            System.out.println(getName() + " received TrackedObjectsEvent at time " + event.getTime());
            if (currentPose != null) {
                List<TrackedObject> trackedObjects = event.getTrackedObjects();
                for (TrackedObject trackedObject : trackedObjects) {
                    List<LandMark> landmarks = fusionSlam.handleTick(trackedObject, currentPose);
                    fusionSlam.updateMap(landmarks);
                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            System.out.println(getName() + " received TerminatedBroadcast from " + c.getSenderId());
            microServiceStatus.put(c.getSenderId(), true);
            if (2 == getNumMicroServices() || c.getSenderId().equals("TimeService")) {
                if (c.getSenderId().equals("TimeService")) {
                    System.out.println("***Times Up Terminating System Getting Output.***");
                } else {
                    System.out.println("***All Operating MicroServices Terminated Terminating System, Creating Output.***");
                }
                try {
                    generateOutput();
                } catch (IOException ex) {
                }
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            System.out.println("***Error detected in " + c.getError().getFaultySensor() + ". Terminating System, Creating Error Output.***");
            generateErrorOutput(errorObject);
            terminate();
        });
    }

    private void generateOutput() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject outputJson = new JsonObject();
        outputJson.addProperty("systemRuntime", statisticalFolder.getSystemRuntime());
        outputJson.addProperty("numDetectedObjects", statisticalFolder.getNumDetectedObjects());
        outputJson.addProperty("numTrackedObjects", statisticalFolder.getNumTrackedObjects());
        outputJson.addProperty("numLandmarks", statisticalFolder.getNumLandmarks());

        // Add landmarks to output JSON
        JsonObject landmarksJson = new JsonObject();
        for (LandMark landmark : fusionSlam.getLandmarks()) {
            JsonObject landmarkJson = new JsonObject();
            landmarkJson.addProperty("id", landmark.getId());
            landmarkJson.addProperty("description", landmark.getDescription());

            JsonArray coordinatesJson = new JsonArray();
            for (CloudPoint point : landmark.getCoordinates()) {
                JsonObject pointJson = new JsonObject();
                pointJson.addProperty("x", point.getX());
                pointJson.addProperty("y", point.getY());
                coordinatesJson.add(pointJson);
            }
            landmarkJson.add("coordinates", coordinatesJson);
            landmarksJson.add(landmark.getId(), landmarkJson);
        }
        outputJson.add("landMarks", landmarksJson);

        // Write to output file
        try (FileWriter writer = new FileWriter("out.json")) {
            gson.toJson(outputJson, writer);
        }
    }

    private void generateErrorOutput(ErrorObject errorObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject errorOutput = new JsonObject();

        // Set error message dynamically based on the description of the object with ID "ERROR"
        String errorMessage = "Unknown error";
        if (errorObject.getFaultySensor().contains("camera")) {
            for (DetectedObject object : statisticalFolder.getDetectedObjects(errorObject.getFaultySensor()).getDetectedObjects()) {
                if (object.getId().equals("ERROR")) {
                    errorMessage = object.getDescription();
                    break;
                }
            }
        } else {
            for (TrackedObject object : statisticalFolder.getLastTrackedObjects()) {
                if (object.getId().equals("ERROR")) {
                    errorMessage = object.getDescription();
                    break;
                }
            }
        }
        errorOutput.addProperty("error", errorMessage);
        errorOutput.addProperty("faultySensor", errorObject.getFaultySensor());

        // Add last cameras frame
        JsonObject lastCamerasFrame = new JsonObject();
        Map<String, StampedDetectedObjects> detectedObjectsMap = StatisticalFolder.getInstance().gDetectedObjects();
        for (Map.Entry<String, StampedDetectedObjects> entry : detectedObjectsMap.entrySet()) {
            JsonObject cameraData = new JsonObject();
            StampedDetectedObjects recentObjects = entry.getValue();
            cameraData.addProperty("time", recentObjects.getTime());
            JsonArray detectedObjectsArray = new JsonArray();
            for (DetectedObject detectedObject : recentObjects.getDetectedObjects()) {
                if (!"ERROR".equals(detectedObject.getId())) {
                    JsonObject detectedObjectJson = new JsonObject();
                    detectedObjectJson.addProperty("id", detectedObject.getId());
                    detectedObjectJson.addProperty("description", detectedObject.getDescription());
                    detectedObjectsArray.add(detectedObjectJson);
                }
            }
            cameraData.add("detectedObjects", detectedObjectsArray);
            lastCamerasFrame.add(entry.getKey(), cameraData);
        }
        errorOutput.add("lastCamerasFrame", lastCamerasFrame);

        // Add last LiDar worker trackers frame
        JsonObject lastLiDarWorkerTrackersFrame = new JsonObject();
        for (LiDarWorkerTracker liDarWorkerTracker : errorObject.getLiDarList()) {
            JsonArray trackedObjectsArray = new JsonArray();
            for (TrackedObject trackedObject : liDarWorkerTracker.getLastTrackedObjects()) {
                if (!"ERROR".equals(trackedObject.getId())) {
                    JsonObject trackedObjectJson = new JsonObject();
                    trackedObjectJson.addProperty("id", trackedObject.getId());
                    trackedObjectJson.addProperty("time", trackedObject.getTime());
                    trackedObjectJson.addProperty("description", trackedObject.getDescription());
                    JsonArray coordinatesArray = new JsonArray();
                    for (CloudPoint point : trackedObject.getCoordinates()) {
                        JsonObject pointJson = new JsonObject();
                        pointJson.addProperty("x", point.getX());
                        pointJson.addProperty("y", point.getY());
                        coordinatesArray.add(pointJson);
                    }
                    trackedObjectJson.add("coordinates", coordinatesArray);
                    trackedObjectsArray.add(trackedObjectJson);
                }
            }
            lastLiDarWorkerTrackersFrame.add("LiDarWorkerTracker" + liDarWorkerTracker.getId(), trackedObjectsArray);
        }
        errorOutput.add("lastLiDarWorkerTrackersFrame", lastLiDarWorkerTrackersFrame);

        // Add poses
        JsonArray posesArray = new JsonArray();
        for (Pose pose : errorObject.getPoseList()) {
            JsonObject poseJson = new JsonObject();
            poseJson.addProperty("time", pose.getTime());
            poseJson.addProperty("x", pose.getX());
            poseJson.addProperty("y", pose.getY());
            poseJson.addProperty("yaw", pose.getYaw());
            posesArray.add(poseJson);
        }
        errorOutput.add("poses", posesArray);

        // Add statistics
        JsonObject statistics = new JsonObject();
        statistics.addProperty("systemRuntime", StatisticalFolder.getInstance().getSystemRuntime());
        statistics.addProperty("numDetectedObjects", StatisticalFolder.getInstance().getNumDetectedObjects());
        statistics.addProperty("numTrackedObjects", StatisticalFolder.getInstance().getNumTrackedObjects());
        statistics.addProperty("numLandmarks", StatisticalFolder.getInstance().getNumLandmarks());

        // Add landmarks
        JsonObject landmarks = new JsonObject();
        for (LandMark landmark : fusionSlam.getLandmarks()) {
            JsonObject landmarkJson = new JsonObject();
            landmarkJson.addProperty("id", landmark.getId());
            landmarkJson.addProperty("description", landmark.getDescription());
            JsonArray coordinatesArray = new JsonArray();
            for (CloudPoint point : landmark.getCoordinates()) {
                JsonObject pointJson = new JsonObject();
                pointJson.addProperty("x", point.getX());
                pointJson.addProperty("y", point.getY());
                coordinatesArray.add(pointJson);
            }
            landmarkJson.add("coordinates", coordinatesArray);
            landmarks.add(landmark.getId(), landmarkJson);
        }
        statistics.add("landMarks", landmarks);
        errorOutput.add("statistics", statistics);

        // Write to error output file
        try (FileWriter writer = new FileWriter("error_output.json")) {
            gson.toJson(errorOutput, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
