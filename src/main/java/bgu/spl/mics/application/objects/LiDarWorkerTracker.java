package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.application.messages.DetectObjectEvent;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker. It processes
 * DetectObjectsEvents and generates TrackedObjectsEvents by using data from the
 * LiDarDataBase. Each worker tracks objects and sends observations to the
 * FusionSlam service.
 */
public class LiDarWorkerTracker {

    private int id;
    private int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;
    private final LiDarDataBase database;
    private final StatisticalFolder statisticalFolder;
    private final String name;

    public LiDarWorkerTracker(int id, int frequency, LiDarDataBase database) {
        this.name = "LiDar" + id;
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new ArrayList<>();
        this.database = database;
        this.statisticalFolder = StatisticalFolder.getInstance();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public void setLastTrackedObjects(List<TrackedObject> lastTrackedObjects) {
        this.lastTrackedObjects = lastTrackedObjects;
    }

    // public TrackedObjectsEvent handleTick(List<DetectedObject> detectedObjects, int currentTick) {
    //     List<TrackedObject> objects = new ArrayList<>();
    //     for (DetectedObject detectedObject : detectedObjects) {
    //         try {
    //             Thread.sleep(frequency);
    //         } catch (InterruptedException e) {
    //             Thread.currentThread().interrupt();
    //             System.err.println("LiDarWorkerTracker " + id + " was interrupted.");
    //             return null;
    //         }
    //         StampedCloudPoints cloudPoints = database.getCloudPointsForObject(detectedObject.getId(), currentTick);
    //         if (cloudPoints != null) {
    //             ArrayList<CloudPoint> points = new ArrayList<>();
    //             points.addAll(cloudPoints.getCloudPoints());
    //             TrackedObject trackedObject = new TrackedObject(
    //                     detectedObject.getId(),
    //                     currentTick,
    //                     detectedObject.getDescription(),
    //                     points
    //             );
    //             objects.add(trackedObject);
    //             statisticalFolder.incrementNumTrackedObjects();
    //         } else {
    //             System.err.println("No cloud points found for ID: " + detectedObject.getId() + " at time: " + currentTick);
    //         }
    //     }
    //     // Update the last tracked objects
    //     TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(objects, currentTick - frequency);
    //     return trackedObjectsEvent;
    // }
    public void addDetectObjects(DetectObjectEvent event) {
        List<StampedCloudPoints> lst = database.getCloudPoints();
        // for (DetectedObject detectedObject : event.getDetectedObjects()) {
        for (StampedCloudPoints stampedCloudPoints : lst) {
            // if ((stampedCloudPoints.getId().equals(detectedObject.getId()) || stampedCloudPoints.getId().equals("ERROR")) && stampedCloudPoints.getTime() == event.getTime()) {
            if (stampedCloudPoints.getTime() == event.getStamp().getTime()) {
                for (DetectedObject detectedObject : event.getStamp().getDetectedObjects()) {
                    if (stampedCloudPoints.getId().equals(detectedObject.getId()) || stampedCloudPoints.getId().equals("ERROR")) {
                        lastTrackedObjects.add(new TrackedObject(stampedCloudPoints.getId(), stampedCloudPoints.getTime(), detectedObject.getDescription(), stampedCloudPoints.getCloudPoints()));
                        statisticalFolder.incrementNumTrackedObjects();

                    }
                }
                // lastTrackedObjects.add(new TrackedObject(stampedCloudPoints.getId(), stampedCloudPoints.getTime(), "ERROR", stampedCloudPoints.getCloudPoints()));
                // statisticalFolder.incrementNumTrackedObjects();

                // }
            }
            // for (DetectedObject detectedObject : event.getDetectedObjects()) {
            //     List<StampedCloudPoints> cloudPoints = database.getCloudPointsForObject(detectedObject.getId(), event.getTime());
            //     for (StampedCloudPoints stampedCloudPoints : cloudPoints) {

            //         if (cloudPoints != null) {
            //             ArrayList<CloudPoint> points = new ArrayList<>();
            //             points.addAll(stampedCloudPoints.getCloudPoints());
            //             TrackedObject trackedObject = new TrackedObject(
            //                     detectedObject.getId(),
            //                     event.getTime(),
            //                     detectedObject.getDescription(),
            //                     points
            //             );
            //             lastTrackedObjects.add(trackedObject);
            //         }
            //     }
            //     statisticalFolder.setLastTrackedObjects(lastTrackedObjects);
            // }
        }
    }

    public List<TrackedObject> getTrackedObjectsAtTime(int time) {

        List<TrackedObject> trackedObjects = new ArrayList<>();
        List<TrackedObject> copy = new ArrayList<>(lastTrackedObjects);
        // System.out.println("LiDarWorkerTracker " + id + " is checking for tracked objects at time: " + time + ", number of tracked objects: " + copy.size());
        for (TrackedObject trackedObject : copy) {
            if (trackedObject.getTime() + frequency <= time) {
                if (trackedObject.getId().equals("ERROR")) {
                    status = STATUS.ERROR;
                    return null;
                }
                trackedObjects.add(trackedObject);
                // statisticalFolder.incrementNumTrackedObjects();
                lastTrackedObjects.remove(trackedObject);
            }
        }

        return trackedObjects;

    }

    public int checkDatabase() {
        return database.getCloudPoints().getLast().getTime();

    }
}
