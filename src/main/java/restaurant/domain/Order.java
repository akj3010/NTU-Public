package restaurant.domain;

/**
 * One customer's order. The price is fixed when the order is taken.
 */
public class Order {
    private final int id;
    private final Customer customer;
    private final MenuItem item;
    private final int lockedPriceCents;

    private OrderStatus status = OrderStatus.PENDING;
    private int unitsDone = 0;

    public Order(int id, Customer customer, MenuItem item, int lockedPriceCents) {
        this.id = id;
        this.customer = customer;
        this.item = item;
        this.lockedPriceCents = lockedPriceCents;
    }

    /** Advances the kitchen by one preparation unit; the last unit makes the order READY. */
    public void advance() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order #" + id + " is not being prepared (status " + status + ").");
        }
        unitsDone++;
        if (unitsDone >= item.getPrepUnits()) {
            status = OrderStatus.READY;
        }
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    public void markServed() {
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        status = OrderStatus.PAID;
    }

    public void cancel() {
        status = OrderStatus.CANCELLED;
    }

    public int remainingUnits() {
        return item.getPrepUnits() - unitsDone;
    }

    public int billCents() {
        return lockedPriceCents;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public String describe() {
        return String.format("#%d %s for %s [%s %d/%d]",
                id, item.getName(), customer.getName(), status, unitsDone, item.getPrepUnits());
    }

    @Override
    public String toString() {
        return describe();
    }
}
