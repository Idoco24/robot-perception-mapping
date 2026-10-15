package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.ErrorObject;

public class CrashedBroadcast implements Broadcast {

    private final ErrorObject error;

    public CrashedBroadcast(ErrorObject Error) {
        this.error = Error;
    }

    public ErrorObject getError() {
        return error;
    }
}
