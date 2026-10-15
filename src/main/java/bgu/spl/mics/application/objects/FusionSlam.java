package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping
 * (SLAM). Combines data from multiple sensors (e.g., LiDAR, camera) to build
 * and update a global map. Implements the Singleton pattern to ensure a single
 * instance of FusionSlam exists.
 */
public class FusionSlam {

    private List<LandMark> landmarks;
    private List<Pose> poses;
    private final StatisticalFolder statisticalFolder;

    // Singleton instance holder
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.statisticalFolder = StatisticalFolder.getInstance();
    }

    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(List<LandMark> landmarks) {
        this.landmarks = landmarks;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public void setPoses(List<Pose> poses) {
        this.poses = poses;
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    public List<LandMark> handleTick(TrackedObject trackedObject, Pose currentPose) {
        List<LandMark> Landmarks = new ArrayList<>();
        List<CloudPoint> globalPoints = new ArrayList<>();

        for (CloudPoint point : trackedObject.getCoordinates()) {
            double globalX = currentPose.getX() + point.getX() * Math.cos(Math.toRadians(currentPose.getYaw())) - point.getY() * Math.sin(Math.toRadians(currentPose.getYaw()));
            double globalY = currentPose.getY() + point.getX() * Math.sin(Math.toRadians(currentPose.getYaw())) + point.getY() * Math.cos(Math.toRadians(currentPose.getYaw()));
            List<Double> coordinates = new ArrayList<>();
            coordinates.add(globalX);
            coordinates.add(globalY);
            globalPoints.add(new CloudPoint(coordinates));
            LandMark landmark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), globalPoints);
            Landmarks.add(landmark);
        }
        return Landmarks;
    }

    public void updateMap(List<LandMark> newLandmarks) {
        for (LandMark newLandmark : newLandmarks) {
            boolean found = false;
            for (LandMark existingLandmark : landmarks) {
                if (existingLandmark.getId().equals(newLandmark.getId())) {
                    // Update existing landmark by averaging coordinates
                    existingLandmark.updateCoordinates(newLandmark.getCoordinates());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Add new landmark
                landmarks.add(newLandmark);
                statisticalFolder.incrementNumLandmarks();
            }
        }
    }

    private static class FusionSlamHolder {

        private static final FusionSlam instance = new FusionSlam();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }
}
