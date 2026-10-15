package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ErrorObject;
import bgu.spl.mics.application.objects.GPSIMU;

/**
 * PoseService is responsible for maintaining the robot's current pose (position
 * and orientation) and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;
    private final ErrorObject errorObject = ErrorObject.getInstance();

    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            System.out.println(getName() + " received TickBroadcast: " + c.getCurrentTick());
            PoseEvent poseEvent = gpsimu.getPoseEvent(c.getCurrentTick());
            if (poseEvent != null) {
                sendEvent(poseEvent);
                errorObject.addPose(poseEvent.getPose());

            } else {
                if (gpsimu.isDone()) {
                    sendBroadcast(new TerminatedBroadcast(getName()));
                    terminate();
                }
                System.out.println(getName() + " has no pose to send at tick " + c.getCurrentTick());
            }

        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            if (c.getSenderId().equals("TimeService")) {
                System.out.println(getName() + " received TerminatedBroadcast from TimeService.");
                sendBroadcast(new TerminatedBroadcast(getName()));
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            System.out.println(getName() + " received CrashedBroadcast.");
            terminate();
        });
    }
}
