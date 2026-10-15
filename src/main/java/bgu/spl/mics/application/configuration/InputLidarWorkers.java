package bgu.spl.mics.application.configuration;

import java.util.List;

import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.StampedCloudPoints;

public class InputLidarWorkers {

    private List<LidarConfiguration> LidarConfigurations;
    private String lidars_data_path;

    public List<LidarConfiguration> getLidarConfigurations() {
        return LidarConfigurations;
    }

    public void setLidarConfigurations(List<LidarConfiguration> lidarConfigurations) {
        LidarConfigurations = lidarConfigurations;
    }

    public String getLidarsDataPath() {
        return lidars_data_path;
    }

    public void setLidarsDataPath(String lidars_data_path) {
        this.lidars_data_path = lidars_data_path;
    }

    public List<StampedCloudPoints> getCloudPointsList() {
        LiDarDataBase lidarDataBase = LiDarDataBase.getInstance(lidars_data_path);
        return lidarDataBase.getCloudPoints();
    }

    public static class LidarConfiguration {

        private int id;
        private int frequency;

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
    }
}
