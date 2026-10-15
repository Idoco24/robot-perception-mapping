package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ErrorObject {

    private final static ErrorObject instance = new ErrorObject();

    private List<CameraError> cameraList = new ArrayList<>();
    private List<LiDarWorkerTracker> liDarList = new ArrayList<>();
    private List<Pose> poseList = new ArrayList<>();
    private String faultySensor;

    public static synchronized ErrorObject getInstance() {
        return instance;
    }

    public void addCamera(Camera camera) {
        cameraList.add(new CameraError(camera));
        faultySensor = camera.getCameraKey();
    }

    public Map<String, StampedDetectedObjects> gDetectedObjects() {
        Map<String, StampedDetectedObjects> cameradetectedObjects = new ConcurrentHashMap<>();
        for (CameraError cameraError : cameraList) {
            cameradetectedObjects.put(cameraError.getCameraKey(), cameraError.getRecentObjects());
        }
        return cameradetectedObjects;
    }

    public void addLiDar(LiDarWorkerTracker liDarWorkerTracker) {
        liDarList.add(liDarWorkerTracker);
        faultySensor = "LiDarWorkerTracker" + liDarWorkerTracker.getId();
    }

    public void addPose(Pose pose) {
        poseList.add(pose);
    }

    public boolean hasErrors() {
        return !cameraList.isEmpty() || !liDarList.isEmpty();
    }

    public String getFaultySensor() {
        return faultySensor;
    }

    public List<CameraError> getCameraList() {
        return cameraList;
    }

    public List<LiDarWorkerTracker> getLiDarList() {
        return liDarList;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

    public class CameraError {

        String cameraKey;
        StampedDetectedObjects recentObjects = new StampedDetectedObjects();

        public CameraError(Camera camera) {
            this.cameraKey = camera.getCameraKey();
            this.recentObjects = camera.getRecentObjects();
        }

        public String getCameraKey() {
            return cameraKey;
        }

        public StampedDetectedObjects getRecentObjects() {
            return recentObjects;
        }
    }
}
