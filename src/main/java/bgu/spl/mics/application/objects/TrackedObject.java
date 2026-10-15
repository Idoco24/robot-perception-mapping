package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR. This object includes information
 * about the tracked object's ID, description, time of tracking, and coordinates
 * in the environment.
 */
public class TrackedObject {

    private String id;
    private int time;
    private String description;
    private List<CloudPoint> coordinates;

    public TrackedObject(String id, int time, String description, List<CloudPoint> coordinates) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }
        this.id = id;
        this.time = time;
        this.description = description != null ? description : "Unknown";
        this.coordinates = coordinates != null ? coordinates : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "Unknown";
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ArrayList<CloudPoint> coordinates) {
        this.coordinates = coordinates != null ? coordinates : new ArrayList<>();
    }
}
