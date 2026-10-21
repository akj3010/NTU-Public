package restaurant.domain;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** First-in-first-out queue of customers waiting for a table. */
public class WaitingQueue {
    private final Deque<Customer> queue = new ArrayDeque<>();

    public void enqueue(Customer customer) {
        queue.addLast(customer);
    }

    public boolean remove(Customer customer) {
        return queue.remove(customer);
    }

    public Customer peek() {
        return queue.peekFirst();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public List<Customer> asList() {
        return List.copyOf(queue);
    }
}
