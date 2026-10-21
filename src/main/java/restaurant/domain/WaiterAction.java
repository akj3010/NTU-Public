package restaurant.domain;

/** The single task the manager may assign to the Waiter each turn. */
public enum WaiterAction {
    SEAT,       // seat one queued customer at a free table
    TAKE_ORDER, // take one seated customer's order
    SERVE,      // serve one READY order
    WAIT        // do nothing
}
