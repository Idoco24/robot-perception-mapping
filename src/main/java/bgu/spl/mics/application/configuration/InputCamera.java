package bgu.spl.mics.application.configuration;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class InputCamera {

    private List<CameraConfiguration> CamerasConfigurations;
    private String camera_datas_path;

    public List<CameraConfiguration> getCamerasConfigurations() {
        return CamerasConfigurations;
    }

    public void setCamerasConfigurations(List<CameraConfiguration> camerasConfigurations) {
        CamerasConfigurations = camerasConfigurations;
    }

    public String getCameraDatasPath() {
        return camera_datas_path;
    }

    public void setCameraDatasPath(String camera_datas_path) {
        this.camera_datas_path = camera_datas_path;
    }

    public List<StampedDetectedObjects> getDetectedObjectsList(String cameraKey) {
        try {
            Gson gson = new Gson();
            try (Reader reader = Files.newBufferedReader(Paths.get(camera_datas_path))) {
                // Read the entire JSON file into a map
                Map<String, List<StampedDetectedObjects>> cameraDataMap = gson.fromJson(reader, new TypeToken<Map<String, List<StampedDetectedObjects>>>() {
                }.getType());
                // Return the list of detected objects for the given camera key
                return cameraDataMap.get(cameraKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class CameraConfiguration {

        private int id;
        private int frequency;
        private String camera_key;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getFrequency() {
            return frequency;
        }

        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }

        public String getCameraKey() {
            return camera_key;
        }

        public void setCameraKey(String camera_key) {
            this.camera_key = camera_key;
        }
    }
}
