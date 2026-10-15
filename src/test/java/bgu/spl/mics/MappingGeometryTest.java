package bgu.spl.mics;

import bgu.spl.mics.application.objects.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MappingGeometryTest {
    private CloudPoint point(double x, double y) {
        return new CloudPoint(List.of(x, y));
    }

    @Test
    void oneTrackedObjectProducesOneLandmarkContainingAllTransformedPoints() {
        TrackedObject object = new TrackedObject("wall", 1, "Wall",
                List.of(point(1, 0), point(0, 2)));
        List<LandMark> landmarks = FusionSlam.getInstance()
                .handleTick(object, new Pose(1, 10, 20, 90));
        assertEquals(1, landmarks.size());
        List<CloudPoint> coordinates = landmarks.get(0).getCoordinates();
        assertEquals(2, coordinates.size());
        assertEquals(10, coordinates.get(0).getX(), 1e-9);
        assertEquals(21, coordinates.get(0).getY(), 1e-9);
        assertEquals(8, coordinates.get(1).getX(), 1e-9);
        assertEquals(20, coordinates.get(1).getY(), 1e-9);
    }

    @Test
    void observationsAverageEachCorrespondingPointAndAppendNewPoints() {
        LandMark landmark = new LandMark("wall", "Wall",
                new ArrayList<>(List.of(point(0, 0), point(10, 10))));
        landmark.updateCoordinates(List.of(point(2, 4), point(14, 18), point(30, 40)));
        assertEquals(3, landmark.getCoordinates().size());
        assertEquals(1, landmark.getCoordinates().get(0).getX(), 1e-9);
        assertEquals(2, landmark.getCoordinates().get(0).getY(), 1e-9);
        assertEquals(12, landmark.getCoordinates().get(1).getX(), 1e-9);
        assertEquals(14, landmark.getCoordinates().get(1).getY(), 1e-9);
        assertEquals(30, landmark.getCoordinates().get(2).getX(), 1e-9);
    }
}
