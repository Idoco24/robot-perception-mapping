package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<Boolean> {
    private final Pose correntPose;

    public PoseEvent(Pose currentPose) {
        this.correntPose = currentPose;
    }

    public Pose getPose() {
        return correntPose;
    }

    public String toString() {
        return correntPose.toString();
    }
}
