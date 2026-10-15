package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {

    private final int currentTick;
    private final int duration;

    public TickBroadcast(int currentTick, int duration) {
        this.currentTick = currentTick;
        this.duration = duration;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public int getDuration() {
        return duration;
    }
}
