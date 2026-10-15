package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectEvent implements Event<Boolean> {

    private final StampedDetectedObjects stamp;

    public DetectObjectEvent(StampedDetectedObjects stamps) {
        this.stamp = stamps;
    }

    public StampedDetectedObjects getStamp() {
        return stamp;
    }
}
