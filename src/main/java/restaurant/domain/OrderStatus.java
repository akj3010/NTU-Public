package restaurant.domain;

/** The lifecycle of a single order as it moves through the kitchen and service. */
public enum OrderStatus {
    PENDING,   // taken by the waiter, being prepared by the chef
    READY,     // all preparation units done; can be served from the next turn
    SERVED,    // delivered to the customer
    PAID,      // bill collected
    CANCELLED  // customer left before paying
}
