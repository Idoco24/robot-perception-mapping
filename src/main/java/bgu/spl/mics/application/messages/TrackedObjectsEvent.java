package bgu.spl.mics.application.messages;

import java.util.List;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * Event representing the detection of tracked objects by the LiDAR.
 */
public class TrackedObjectsEvent implements Event<Boolean> {

    private List<TrackedObject> trackedObjects;
    private int time;

    public TrackedObjectsEvent(List<TrackedObject> trackedObjects, int time) {
        this.trackedObjects = trackedObjects;
        this.time = time;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    public void setTrackedObjects(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
