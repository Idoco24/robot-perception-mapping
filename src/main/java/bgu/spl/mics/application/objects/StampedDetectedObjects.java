package bgu.spl.mics.application.objects;

import java.util.Collections;
import java.util.List;

public class StampedDetectedObjects {

    private int time;
    private List<DetectedObject> detectedObjects;

    public StampedDetectedObjects() {
        this.time = 0;
        this.detectedObjects = Collections.emptyList();
    }

    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        if (detectedObjects == null) {
            this.detectedObjects = Collections.emptyList();
        } else {
            this.detectedObjects = detectedObjects;
        }
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    public void setDetectedObjects(List<DetectedObject> detectedObjects) {
        this.detectedObjects = detectedObjects;
    }
}
