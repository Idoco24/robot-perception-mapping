package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the
 * LiDAR. These points are used to generate a point cloud representing objects
 * in the environment.
 */
public class CloudPoint {

    private Double x;
    private Double y;

    public CloudPoint(List<Double> cloudPoint) {
        this.x = cloudPoint.get(0);
        this.y = cloudPoint.get(1);
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
