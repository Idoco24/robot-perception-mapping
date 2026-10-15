package bgu.spl.mics.application.configuration;

public class InputFile {

    private InputCamera Cameras;
    private InputLidarWorkers LiDarWorkers; // Change this to match the JSON field name
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    public InputCamera getCameras() {
        return Cameras;
    }

    public void setCameras(InputCamera cameras) {
        Cameras = cameras;
    }

    public InputLidarWorkers getLidarWorkers() {
        return LiDarWorkers; // Change this to match the JSON field name
    }

    public void setLidarWorkers(InputLidarWorkers lidars) {
        LiDarWorkers = lidars; // Change this to match the JSON field name
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public void setPoseJsonFile(String poseJsonFile) {
        this.poseJsonFile = poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public void setTickTime(int TickTime) {
        this.TickTime = TickTime;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int Duration) {
        this.Duration = Duration;
    }
}
