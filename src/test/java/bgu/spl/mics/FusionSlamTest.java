package bgu.spl.mics;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;

class FusionSlamTest {

    private FusionSlam fusionSlam;
    private Pose pose;
    private TrackedObject trackedObject;

    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();
        pose = new Pose(0, 0, 0, 0);
        List<CloudPoint> points = new ArrayList<>();
        List<Double> coordinates = new ArrayList<>();
        coordinates.add(1.0);
        coordinates.add(1.0);
        points.add(new CloudPoint(coordinates));
        trackedObject = new TrackedObject("1", 0, "TestObject", points);
    }

    @Test
    void testHandleTick() {
        // @Pre: FusionSlam is initialized and has a tracked object and pose
        assertEquals(1, trackedObject.getCoordinates().size());

        // Action: Handle tick with the tracked object and pose
        List<LandMark> landmarks = fusionSlam.handleTick(trackedObject, pose);

        // @INV: The landmarks list should contain the transformed tracked object
        assertEquals(1, landmarks.size());
        LandMark landmark = landmarks.get(0);
        assertEquals("1", landmark.getId());
        assertEquals("TestObject", landmark.getDescription());
        assertEquals(1, landmark.getCoordinates().size());
        assertEquals(1.0, landmark.getCoordinates().get(0).getX());
        assertEquals(1.0, landmark.getCoordinates().get(0).getY());
    }

    @Test
    void testHandleMultipleTicks() {
        // @Pre: FusionSlam is initialized and has multiple tracked objects and poses
        List<CloudPoint> points2 = new ArrayList<>();
        List<Double> coordinates2 = new ArrayList<>();
        coordinates2.add(2.0);
        coordinates2.add(2.0);
        points2.add(new CloudPoint(coordinates2));
        TrackedObject trackedObject2 = new TrackedObject("2", 0, "TestObject2", points2);

        assertEquals(1, trackedObject.getCoordinates().size());
        assertEquals(1, trackedObject2.getCoordinates().size());

        // Action: Handle tick with multiple tracked objects and poses
        List<LandMark> landmarks1 = fusionSlam.handleTick(trackedObject, pose);
        List<LandMark> landmarks2 = fusionSlam.handleTick(trackedObject2, pose);

        // @INV: The landmarks list should contain the transformed tracked objects
        assertEquals(1, landmarks1.size());
        assertEquals(1, landmarks2.size());

        LandMark landmark1 = landmarks1.get(0);
        LandMark landmark2 = landmarks2.get(0);

        assertEquals("1", landmark1.getId());
        assertEquals("TestObject", landmark1.getDescription());
        assertEquals(1, landmark1.getCoordinates().size());
        assertEquals(1.0, landmark1.getCoordinates().get(0).getX());
        assertEquals(1.0, landmark1.getCoordinates().get(0).getY());

        assertEquals("2", landmark2.getId());
        assertEquals("TestObject2", landmark2.getDescription());
        assertEquals(1, landmark2.getCoordinates().size());
        assertEquals(2.0, landmark2.getCoordinates().get(0).getX());
        assertEquals(2.0, landmark2.getCoordinates().get(0).getY());
    }
}
