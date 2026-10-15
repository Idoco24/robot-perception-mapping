package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import com.google.gson.Gson;

import bgu.spl.mics.application.messages.PoseEvent;

/**
 * Represents the robot's GPS and IMU system. It provides the robot's pose data
 * at each tick.
 */
public class GPSIMU {

    private int currentTick;
    private List<Pose> poseList;
    private boolean done = false;

    public GPSIMU(String poseJsonFile) {
        try (Reader reader = new FileReader(poseJsonFile)) {
            Gson gson = new Gson();
            Pose[] poses = gson.fromJson(reader, Pose[].class);
            this.poseList = List.of(poses);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public PoseEvent getPoseEvent(int tick) {
        setCurrentTick(tick);
        for (Pose pose : poseList) {
            if (pose.getTime() == tick) {
                return new PoseEvent(pose);
            }
        }
        setDone();
        return null;

    }

    public boolean isDone() {
        return done;
    }

    public void setDone() {
        if (poseList.getLast().getTime() < currentTick) {
            done = true;
        }
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

}
