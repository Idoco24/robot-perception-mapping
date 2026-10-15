package bgu.spl.mics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MessageBusImplTest {

    private MessageBusImpl messageBus;
    private MicroService microService1;
    private MicroService microService2;

    @BeforeEach
    void setUp() {
        messageBus = MessageBusImpl.getInstance();
        microService1 = new MicroService("MicroService1") {
            @Override
            protected void initialize() {
            }
        };
        microService2 = new MicroService("MicroService2") {
            @Override
            protected void initialize() {
            }
        };
    }

    @Test
    void testRegisterAndUnregisterMicroService() {
        // @Pre: MessageBus is initialized and empty
        assertEquals(0, messageBus.getNumMicroS());

        // Action: Register a microservice
        messageBus.register(microService1);

        // @INV: The number of registered microservices should be 1
        assertEquals(1, messageBus.getNumMicroS());

        // Action: Unregister the microservice
        messageBus.unregister(microService1);

        // @Post: The number of registered microservices should be 0
        assertEquals(0, messageBus.getNumMicroS());
    }

    @AfterEach
    void removeTestSubscriptions() {
        messageBus.unregister(microService1);
        messageBus.unregister(microService2);
    }

    @Test
    void testSendEvent() throws InterruptedException {
        // @Pre: MessageBus is initialized and microService1 is registered
        messageBus.register(microService1);
        messageBus.subscribeEvent(TestEvent.class, microService1);

        // Action: Send an event
        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);

        // @INV: The future object should not be null and should not be done
        assertNotNull(future);
        assertFalse(future.isDone());

        // Action: Await the message
        Message receivedMessage = messageBus.awaitMessage(microService1);

        // @INV: The received message should be an instance of TestEvent
        assertTrue(receivedMessage instanceof TestEvent);

        // Action: Complete the event
        messageBus.complete(event, "TestResult");

        // @Post: The future object should be done and have the correct result
        assertTrue(future.isDone());
        assertEquals("TestResult", future.get());
    }

    @Test
    void testSendBroadcast() throws InterruptedException {
        // @Pre: MessageBus is initialized and microService1 and microService2 are registered
        messageBus.register(microService1);
        messageBus.register(microService2);
        messageBus.subscribeBroadcast(TestBroadcast.class, microService1);
        messageBus.subscribeBroadcast(TestBroadcast.class, microService2);

        // Action: Send a broadcast
        TestBroadcast broadcast = new TestBroadcast();
        messageBus.sendBroadcast(broadcast);

        // Action: Await the message for both microservices
        Message receivedMessage1 = messageBus.awaitMessage(microService1);
        Message receivedMessage2 = messageBus.awaitMessage(microService2);

        // @Post: Both received messages should be instances of TestBroadcast
        assertTrue(receivedMessage1 instanceof TestBroadcast);
        assertTrue(receivedMessage2 instanceof TestBroadcast);
    }

    private static class TestEvent implements Event<String> {
    }

    private static class TestBroadcast implements Broadcast {
    }
}
