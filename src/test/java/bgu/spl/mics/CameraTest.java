package bgu.spl.mics;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.messages.DetectObjectEvent;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

class CameraTest {

    private Camera camera;
    private List<StampedDetectedObjects> detectedObjects;

    @BeforeEach
    void setUp() {
        detectedObjects = new ArrayList<>();
        List<DetectedObject> objects = new ArrayList<>();
        objects.add(new DetectedObject("1", "TestObject"));

        StampedDetectedObjects stampedDetectedObjects = new StampedDetectedObjects();
        stampedDetectedObjects.setTime(0);
        stampedDetectedObjects.setDetectedObjects(objects);
        detectedObjects.add(stampedDetectedObjects);
        camera = new Camera(1, 1, "Camera1", detectedObjects);
    }

    @Test
    void testGetObjectsToSend() {
        // @Pre: Camera is initialized with detected objects
        assertEquals(1, detectedObjects.size());

        // Action: Get objects to send at tick 1
        DetectObjectEvent event = camera.getObjectsToSend(1);

        // @INV: The event should contain the detected objects
        assertEquals(1, event.getStamp().getDetectedObjects().size());
        assertEquals("1", event.getStamp().getDetectedObjects().get(0).getId());
        assertEquals("TestObject", event.getStamp().getDetectedObjects().get(0).getDescription());
    }

    @Test
    void testGetObjectsToSendAtDifferentTime() {
        // @Pre: Camera is initialized with detected objects
        assertEquals(1, detectedObjects.size());

        // Action: Get objects to send at tick 1 (since frequency is 1, it should match the time)
        DetectObjectEvent event = camera.getObjectsToSend(1);

        // @INV: The event should contain the detected objects
        assertEquals(1, event.getStamp().getDetectedObjects().size());
        assertEquals("1", event.getStamp().getDetectedObjects().get(0).getId());
        assertEquals("TestObject", event.getStamp().getDetectedObjects().get(0).getDescription());
    }

    @Test
    void testGetObjectsToSendMultipleTicks() {
        // @Pre: Camera is initialized with detected objects
        assertEquals(1, detectedObjects.size());

        // Add more detected objects at different times
        List<DetectedObject> objects2 = new ArrayList<>();
        objects2.add(new DetectedObject("2", "TestObject2"));
        StampedDetectedObjects stampedDetectedObjects2 = new StampedDetectedObjects();
        stampedDetectedObjects2.setTime(2);
        stampedDetectedObjects2.setDetectedObjects(objects2);
        detectedObjects.add(stampedDetectedObjects2);

        List<DetectedObject> objects3 = new ArrayList<>();
        objects3.add(new DetectedObject("3", "TestObject3"));
        StampedDetectedObjects stampedDetectedObjects3 = new StampedDetectedObjects();
        stampedDetectedObjects3.setTime(4);
        stampedDetectedObjects3.setDetectedObjects(objects3);
        detectedObjects.add(stampedDetectedObjects3);

        // Action: Get objects to send at tick 3 (frequency is 1, so it should match the time)
        DetectObjectEvent event2 = camera.getObjectsToSend(3);

        // @INV: The event should contain the detected objects for time 2
        assertEquals(1, event2.getStamp().getDetectedObjects().size());
        assertEquals("2", event2.getStamp().getDetectedObjects().get(0).getId());
        assertEquals("TestObject2", event2.getStamp().getDetectedObjects().get(0).getDescription());

        // Action: Get objects to send at tick 5 (frequency is 1, so it should match the time)
        DetectObjectEvent event4 = camera.getObjectsToSend(5);

        // @INV: The event should contain the detected objects for time 4
        assertEquals(1, event4.getStamp().getDetectedObjects().size());
        assertEquals("3", event4.getStamp().getDetectedObjects().get(0).getId());
        assertEquals("TestObject3", event4.getStamp().getDetectedObjects().get(0).getDescription());
    }
}
