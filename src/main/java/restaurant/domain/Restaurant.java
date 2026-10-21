package restaurant.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

/**
 * The whole world state of the game (aggregate root). It OWNS its tables, waiting queue,
 * menu and staff, and tracks the running totals used for the objective check.
 */
public class Restaurant {
    private final Menu menu;

    private final List<Table> tables = new ArrayList<>();
    private final WaitingQueue waiting = new WaitingQueue();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();

    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final List<Staff> staff;

    private int turn;
    private int revenueCents;
    private int servedCount;
    private int paidCount;
    private int unhappyDepartures;
    private int paidSatisfactionSum;
    private int nextOrderId = 1;

    public Restaurant(Menu menu) {
        this.menu = menu;
        this.tables.add(new Table(1));
        this.tables.add(new Table(2));
        this.waiter = new Waiter();
        this.chef = new Chef();
        this.cashier = new Cashier();
        this.staff = List.of(waiter, chef, cashier);
    }

    // ------------------------------------------------------------------
    // Customers and the waiting queue
    // ------------------------------------------------------------------

    public void addCustomer(Customer customer) {
        customers.add(customer);
        waiting.enqueue(customer);
    }

    public void removeFromWaiting(Customer customer) {
        waiting.remove(customer);
    }

    public WaitingQueue getWaiting() {
        return waiting;
    }

    public List<Customer> allCustomers() {
        return List.copyOf(customers);
    }

    /** Customers still in play (not PAID and not LEFT). */
    public List<Customer> activeCustomers() {
        return customers.stream()
                .filter(c -> c.getState() != CustomerState.PAID && c.getState() != CustomerState.LEFT)
                .toList();
    }

    public Customer findCustomer(int id) {
        return customers.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No customer #" + id));
    }

    // ------------------------------------------------------------------
    // Tables
    // ------------------------------------------------------------------

    public List<Table> getTables() {
        return List.copyOf(tables);
    }

    public Table firstFreeTable() {
        return tables.stream().filter(Table::isFree).findFirst().orElse(null);
    }

    public boolean hasFreeTable() {
        return firstFreeTable() != null;
    }

    // ------------------------------------------------------------------
    // Orders
    // ------------------------------------------------------------------

    public Order createOrder(Customer customer, MenuItem item, int priceCents) {
        Order order = new Order(nextOrderId++, customer, item, priceCents);
        orders.add(order);
        customer.setOrder(order);
        return order;
    }

    public List<Order> allOrders() {
        return List.copyOf(orders);
    }

    /** Orders still in the kitchen or waiting to be served (PENDING or READY). */
    public List<Order> openOrders() {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.READY)
                .toList();
    }

    public Order findOrder(int id) {
        return orders.stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No order #" + id));
    }

    // ------------------------------------------------------------------
    // Running totals and objective data
    // ------------------------------------------------------------------

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public void addRevenue(int cents) {
        revenueCents += cents;
    }

    public int getServedCount() {
        return servedCount;
    }

    public void recordServing() {
        servedCount++;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public void recordPaid(int satisfaction) {
        paidCount++;
        paidSatisfactionSum += satisfaction;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public void recordUnhappyExit() {
        unhappyDepartures++;
    }

    /** Average satisfaction of paid customers, empty until the first payment. */
    public OptionalDouble averagePaidSatisfaction() {
        return paidCount == 0 ? OptionalDouble.empty() : OptionalDouble.of((double) paidSatisfactionSum / paidCount);
    }

    // ------------------------------------------------------------------
    // Staff
    // ------------------------------------------------------------------

    public Waiter getWaiter() {
        return waiter;
    }

    public Chef getChef() {
        return chef;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public List<Staff> getStaff() {
        return staff;
    }

    public void resetStaffTurn() {
        staff.forEach(Staff::beginTurn);
    }

    /** The oldest READY_TO_PAY customer that became ready on an earlier turn. */
    public Customer oldestReadyToPay(int currentTurn) {
        return customers.stream()
                .filter(c -> c.getState() == CustomerState.READY_TO_PAY && c.getReadyToPayTurn() < currentTurn)
                .min(Comparator.comparingInt(Customer::getReadyToPayTurn).thenComparingInt(Customer::getId))
                .orElse(null);
    }

    public Menu getMenu() {
        return menu;
    }
}
