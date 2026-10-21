package restaurant.domain;

/**
 * The cashier automatically collects at most one bill per turn (oldest READY_TO_PAY
 * customer first). Payment adds the bill to revenue, records the satisfaction for the
 * average, marks the customer PAID and frees the table.
 */
public class Cashier extends Staff {

    public Cashier() {
        super("Cashier");
    }

    public int collectPayment(Customer customer, Restaurant restaurant) {
        Order order = customer.getOrder();
        int amount = (order != null) ? order.billCents() : 0;
        if (order != null) {
            order.markPaid();
        }
        restaurant.addRevenue(amount);
        restaurant.recordPaid(customer.getSatisfaction());
        customer.setState(CustomerState.PAID);
        Table table = customer.getTable();
        if (table != null) {
            table.vacate();
            customer.setTable(null);
        }
        return amount;
    }
}
