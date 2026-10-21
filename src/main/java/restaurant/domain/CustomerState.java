package restaurant.domain;

/** The lifecycle a customer moves through during the game. */
public enum CustomerState {
    WAITING,      // in the queue, waiting for a table
    SEATED,       // at a table, may or may not have ordered yet
    EATING,       // order served, eating (loses no satisfaction)
    READY_TO_PAY, // finished eating, waiting for the cashier
    PAID,         // paid and left
    LEFT          // abandoned without paying
}
