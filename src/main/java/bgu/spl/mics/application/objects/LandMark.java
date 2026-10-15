package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map. Landmarks are identified and
 * updated by the FusionSlam service.
 */
public class LandMark {

    private String Id;
    private String Description;
    private List<CloudPoint> Coordinates;

    public LandMark(String Id, String Description, List<CloudPoint> Coordinates) {
        this.Id = Id;
        this.Description = Description;
        this.Coordinates = Coordinates;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public List<CloudPoint> getCoordinates() {
        return Coordinates;
    }

    public void setCoordinates(List<CloudPoint> coordinates) {
        Coordinates = coordinates;
    }

    public void updateCoordinates(List<CloudPoint> newCoordinates) {
        // Average the new coordinates with the existing ones
        for (int i = 0; i < newCoordinates.size(); i++) {
            CloudPoint newPoint = newCoordinates.get(i);
            if (i < Coordinates.size()) {
                CloudPoint existingPoint = Coordinates.getLast();
                double avgX = (existingPoint.getX() + newPoint.getX()) / 2;
                double avgY = (existingPoint.getY() + newPoint.getY()) / 2;
                existingPoint.setX(avgX);
                existingPoint.setY(avgY);
            } else {
                Coordinates.add(newPoint);

            }
        }
    }
}
