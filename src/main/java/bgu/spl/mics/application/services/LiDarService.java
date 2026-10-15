package bgu.spl.mics.application.services;

import java.util.List;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.ErrorObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.TrackedObject;

public class LiDarService extends MicroService {

    private final LiDarWorkerTracker LiDar;
    private int currentTick = 0;
    private ErrorObject errorObject = ErrorObject.getInstance();

    public LiDarService(LiDarWorkerTracker liDarTracker) {
        super("LiDarWorker" + liDarTracker.getId());
        this.LiDar = liDarTracker;
        errorObject.addLiDar(LiDar);
    }

    @Override
    public void initialize() {
        System.out.println(getName() + " initialized.");

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            // Handle TickBroadcast
            System.out.println(getName() + " received TickBroadcast: " + c.getCurrentTick());
            if (LiDar.getStatus() == STATUS.UP) {
                List<TrackedObject> trackedObjects = LiDar.getTrackedObjectsAtTime(c.getCurrentTick());
                if (trackedObjects.isEmpty()) {
                    if (LiDar.checkDatabase() < c.getCurrentTick()) {
                        LiDar.setStatus(STATUS.DOWN);
                        sendBroadcast(new TerminatedBroadcast(getName()));
                        terminate();
                    }
                    System.out.println(getName() + " has no objects to send at tick " + c.getCurrentTick());
                    // return;
                }
                TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObjects, c.getCurrentTick());
                sendEvent(event);
            }
            if (LiDar.getStatus() == STATUS.ERROR) {
                System.out.println(getName() + " detected an error in objects.");
                errorObject.addLiDar(LiDar);
                sendBroadcast(new CrashedBroadcast(errorObject));
                terminate();
            }

        });

        subscribeEvent(DetectObjectEvent.class, (DetectObjectEvent event) -> {
            LiDar.addDetectObjects(event);

        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            System.out.println(getName() + " received TerminatedBroadcast.");
            if (c.getSenderId().equals("TimeService")) {
                LiDar.setStatus(STATUS.DOWN);
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
