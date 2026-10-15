package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl} class is the implementation of the MessageBus
 * interface. Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private final Map<Class<? extends Event>, BlockingQueue<MicroService>> eventMessages;
    private final Map<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcastMessages;
    private final Map<Event, Future> futureMap;
    private final Map<MicroService, BlockingQueue<Message>> microServiceQueues;

    private static final MessageBusImpl instance = new MessageBusImpl();

    private MessageBusImpl() {
        eventMessages = new ConcurrentHashMap<>();
        futureMap = new ConcurrentHashMap<>();
        broadcastMessages = new ConcurrentHashMap<>();
        microServiceQueues = new ConcurrentHashMap<>();
    }

    public static MessageBusImpl getInstance() {
        return instance;
    }

    @Override
    public void register(MicroService m) {
        microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(MicroService m) {
        microServiceQueues.remove(m);
        eventMessages.values().forEach(queue -> queue.remove(m));
        broadcastMessages.values().forEach(queue -> queue.remove(m));
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        eventMessages.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).add(m);
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        BlockingQueue<MicroService> subscribers = eventMessages.get(e.getClass());

        if (subscribers != null && !subscribers.isEmpty()) {
            MicroService subscriber;
            synchronized (subscribers) {
                subscriber = subscribers.poll();
                subscribers.add(subscriber);
            }

            BlockingQueue<Message> queue = microServiceQueues.get(subscriber);
            if (queue != null) {
                Future<T> future = new Future<>();
                futureMap.put(e, future);
                try {
                    queue.put(e);
                    // System.out.println("Event sent: " + e.getClass().getName());

                } catch (InterruptedException ex) {
                }
                return future;
            }
        }
        return null;
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        broadcastMessages.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> future = (Future<T>) futureMap.remove(e);
        if (future != null) {
            future.resolve(result);
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        BlockingQueue<MicroService> subscribers = broadcastMessages.get(b.getClass());
        if (subscribers != null && !subscribers.isEmpty()) {
            for (MicroService m : subscribers) {
                BlockingQueue<Message> queue = microServiceQueues.get(m);
                if (queue != null) {
                    try {
                        queue.put(b);
                        // System.out.println("Broadcast sent: " + b.getClass().getName());

                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        BlockingQueue<Message> queue = microServiceQueues.get(m);

        if (queue == null) {
            throw new IllegalStateException("MicroService " + m.getName() + " is not registered.");
        }

        // System.out.println("MicroService " + m.getName() + " is waiting for a message.");
        Message message = queue.take();
        // System.out.println("MicroService " + m.getName() + " received a message: " + message.getClass().getName());
        return message;
    }

    @Override
    public int getNumMicroS() {
        return microServiceQueues.size();
    }
}
