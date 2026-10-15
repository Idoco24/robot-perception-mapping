package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LiDarDataBase {

    private List<StampedCloudPoints> cloudPoints;
    private static LiDarDataBase instance = null;

    public LiDarDataBase(List<StampedCloudPoints> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }

    public synchronized List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }

    public void setCloudPoints(List<StampedCloudPoints> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }

    public synchronized List<StampedCloudPoints> getCloudPointsForObject(String id, int time) {
        if (id == null || id.isEmpty()) {
            System.err.println("Invalid ID: " + id);
            return null;
        }

        List<StampedCloudPoints> cloudPointsList = new ArrayList<>();

        // Iterate through the list of StampedCloudPoints to find a match.
        for (StampedCloudPoints stampedPoint : cloudPoints) {
            // Check if the id and time match.
            if (stampedPoint.getId().equals(id) && stampedPoint.getTime() == time) {
                cloudPointsList.add(stampedPoint); // Return the matching StampedCloudPoints.
            }
        }
        return cloudPointsList;

        // Log in case no match is found and return null.
        // System.out.println("No cloud points found for ID: " + id + " at time: " + time);
        // return null;
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static synchronized LiDarDataBase getInstance(String filePath) {
        if (instance != null) {
            return instance;
        }
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            // Define the type for the list of employees
            Type StampedCloudPointsListType = new TypeToken<List<StampedCloudPoints>>() {
            }.getType();
            // Deserialize JSON to list of employees
            List<StampedCloudPoints> StampedCloudPointsList = gson.fromJson(reader,
                    StampedCloudPointsListType);
            // Use the employee data
            instance = new LiDarDataBase(StampedCloudPointsList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instance;
    }
}
