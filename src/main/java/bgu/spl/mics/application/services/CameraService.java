package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.ErrorObject;
import bgu.spl.mics.application.objects.STATUS;

public class CameraService extends MicroService {

    private final Camera camera;
    private final ErrorObject errorObject = ErrorObject.getInstance();

    public CameraService(Camera camera) {
        super(camera.getCameraKey());
        this.camera = camera;

    }

    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast c) -> {
            System.out.println(getName() + " received TickBroadcast: " + c.getCurrentTick());
            DetectObjectEvent event = camera.getObjectsToSend(c.getCurrentTick());
            if (event.getStamp().getDetectedObjects().isEmpty()) {
                if (camera.getDetectedObjectsList().getLast().getTime() < c.getCurrentTick()) {
                    camera.setSTATUS(STATUS.DOWN);
                    sendBroadcast(new TerminatedBroadcast(getName()));
                    terminate();
                }

                System.out.println(getName() + " has no objects to send at tick " + c.getCurrentTick());
                return;
            }

            boolean hasError = event.getStamp().getDetectedObjects().stream()
                    .anyMatch(detectedObject -> "ERROR".equals(detectedObject.getId()));

            if (hasError) {
                camera.setSTATUS(STATUS.ERROR);
                System.out.println(getName() + " detected an error in objects.");
                errorObject.addCamera(camera);
                sendBroadcast(new CrashedBroadcast(errorObject));
                terminate();
            }
            sendEvent(event);

        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            if (c.getSenderId().equals("TimeService")) {
                System.out.println(getName() + " received TerminatedBroadcast.");
                camera.setSTATUS(STATUS.DOWN);
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
