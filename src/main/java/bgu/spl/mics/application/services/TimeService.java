package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService is responsible for sending TickBroadcasts at regular intervals
 * and terminating the system after the specified duration.
 */
public class TimeService extends MicroService {

    private final int tickTime;
    private final int duration;
    private int currentTick;

    private final StatisticalFolder statisticalFolder;

    public TimeService(int tickTime, int duration) {
        super("TimeService");
        this.tickTime = tickTime;
        this.duration = duration;
        this.currentTick = 0;
        this.statisticalFolder = StatisticalFolder.getInstance();
    }

    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast c) -> {
            this.terminate();
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast c) -> {
            if (c.getSenderId().equals("FusionSlamService")) {
                terminate();
                sendBroadcast(new TerminatedBroadcast(getName()));
            }
        });

        Thread tickThread = new Thread(() -> {
            while (currentTick < duration && !getTerminate()) {
                try {
                    sendBroadcast(new TickBroadcast(currentTick, duration));
                    statisticalFolder.incrementSystemRuntime();
                    currentTick++;
                    Thread.sleep(tickTime * 1000);
                    System.out.println(getName() + " sent TickBroadcast: " + currentTick);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
            sendBroadcast(new TerminatedBroadcast(getName()));
            terminate();
        });
        tickThread.start();
    }
}
