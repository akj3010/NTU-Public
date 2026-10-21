package restaurant.domain;

import java.util.Random;

/**
 * The waiter performs at most one task per turn: seat a queued customer, take one seated
 * customer's order, or serve one READY order.
 */
public class Waiter extends Staff {

    public Waiter() {
        super("Waiter");
    }

    public void seat(Customer customer, Table table, int turn) {
        table.seat(customer);
        customer.setTable(table);
        customer.setState(CustomerState.SEATED);
        customer.setSeatedTurn(turn);
    }

    /**
     * Takes a customer's order: the CUSTOMER chooses the dish (never the manager) and the
     * order is registered with the restaurant at the dish's full price.
     */
    public Order takeOrder(Customer customer, Menu menu, Random rng, Restaurant restaurant) {
        MenuItem item = customer.chooseItem(menu, rng);
        return restaurant.createOrder(customer, item, item.getPriceCents());
    }

    /** Serves a READY order. */
    public void serve(Order order, int turn) {
        order.markServed();
        Customer customer = order.getCustomer();
        customer.setState(CustomerState.EATING);
        customer.setServedTurn(turn);
    }
}
