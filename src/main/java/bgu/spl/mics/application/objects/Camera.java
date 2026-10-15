package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.application.messages.DetectObjectEvent;

/**
 * Represents a camera sensor on the robot. Responsible for detecting objects in
 * the environment.
 */
public class Camera {

    private int id;
    private int frequency;
    private String CameraKey;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjects;
    private final StatisticalFolder statisticalFolder;

    private StampedDetectedObjects recentObjects;

    public Camera(int id, int frequency, String CameraKey, List<StampedDetectedObjects> detectedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.CameraKey = CameraKey;
        if (detectedObjects == null) {
            this.detectedObjects = new ArrayList<>();
        } else {
            this.detectedObjects = detectedObjects;
        }
        this.statisticalFolder = StatisticalFolder.getInstance();
    }

    public StampedDetectedObjects getRecentObjects() {
        return recentObjects;
    }

    public void setRecentObjects(StampedDetectedObjects recentObjects) {
        this.recentObjects = recentObjects;
    }

    public int getId() {
        return id;
    }

    public String getCameraKey() {
        return CameraKey;
    }

    public void setCameraKey(String cameraKey) {
        CameraKey = cameraKey;
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

    public STATUS getSTATUS() {
        return status;
    }

    public void setSTATUS(STATUS status) {
        this.status = status;
    }

    public List<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjects;
    }

    public void setDetectedObjectsList(List<StampedDetectedObjects> detectedObjectsList) {
        this.detectedObjects = detectedObjectsList;
    }

    public DetectObjectEvent getObjectsToSend(int currentTick) {
        StampedDetectedObjects objects = new StampedDetectedObjects();

        // Filter detected objects and convert to DetectObjectEvent
        for (StampedDetectedObjects stampedObject : detectedObjects) {
            // Check if the current tick is a multiple of the frequency and matches the stamped object's time
            if (stampedObject != null && stampedObject.getDetectedObjects() != null && !stampedObject.getDetectedObjects().isEmpty()) {
                if (currentTick == stampedObject.getTime() + frequency) {

                    objects = stampedObject;
                    setRecentObjects(stampedObject);
                    statisticalFolder.addDetectedObjects(CameraKey, stampedObject);
                    statisticalFolder.incrementNumDetectedObjects(objects.getDetectedObjects().size());
                    break;
                }
            }
        }
        return new DetectObjectEvent(objects);
    }

    @Override
    public String toString() {
        return "Camera [id=" + id + ", frequency=" + frequency + ", CameraKey=" + CameraKey + ", status=" + status
                + ", detectedObjects=" + detectedObjects + "]";
    }
}
