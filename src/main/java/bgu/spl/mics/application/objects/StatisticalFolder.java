package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds statistical information about the system's operation. This class
 * aggregates metrics such as the runtime of the system, the number of objects
 * detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    private static StatisticalFolder instance;

    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;

    private Map<String, StampedDetectedObjects> detectedObjects;
    private List<TrackedObject> lastTrackedObjects;

    private StatisticalFolder() {
        this.systemRuntime = 0;
        this.numDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;

        this.detectedObjects = new ConcurrentHashMap<>();
        this.lastTrackedObjects = new ArrayList<>();

    }

    public Map<String, StampedDetectedObjects> gDetectedObjects() {
        return detectedObjects;
    }

    public StampedDetectedObjects getDetectedObjects(String cameraKey) {
        return detectedObjects.get(cameraKey);
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public void setLastTrackedObjects(List<TrackedObject> lastTrackedObjects) {
        this.lastTrackedObjects = lastTrackedObjects;
    }

    public void addDetectedObjects(String key, StampedDetectedObjects detectedObjects) {
        this.detectedObjects.put(key, detectedObjects);
    }

    public static synchronized StatisticalFolder getInstance() {
        if (instance == null) {
            instance = new StatisticalFolder();
        }
        return instance;
    }

    public synchronized void incrementSystemRuntime() {
        systemRuntime++;
    }

    public synchronized void incrementNumDetectedObjects(int newDetectedObjects) {
        numDetectedObjects += newDetectedObjects;
    }

    public synchronized void incrementNumTrackedObjects() {
        numTrackedObjects++;
    }

    public synchronized void incrementNumLandmarks() {
        numLandmarks++;
    }

    public synchronized int getSystemRuntime() {
        return systemRuntime;
    }

    public synchronized int getNumDetectedObjects() {
        return numDetectedObjects;
    }

    public synchronized int getNumTrackedObjects() {
        return numTrackedObjects;
    }

    public synchronized int getNumLandmarks() {
        return numLandmarks;
    }
}
