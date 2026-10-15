package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * Broadcast sent when the system terminates successfully. Used to notify all
 * services to clean up and terminate their execution.
 */
public class TerminatedBroadcast implements Broadcast {

    private final String terminationMessage;
    private final String senderId;

    public TerminatedBroadcast(String name) {
        this.terminationMessage = "terminated";
        this.senderId = name;
    }

    /**
     * Retrieves the termination message.
     *
     * @return The termination message.
     */
    public String getTerminationMessage() {
        return terminationMessage;
    }

    /**
     * Retrieves the sender's ID.
     *
     * @return The sender's ID.
     */
    public String getSenderId() {
        return senderId;

    }
}
