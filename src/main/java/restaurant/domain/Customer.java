package restaurant.domain;

import java.util.List;
import java.util.Random;

/**
 * A customer in the restaurant (Stage 1: regular customers).
 *
 * The order choice and the satisfaction loss are abstract methods even though Stage 1 has
 * only one customer type: that is the seam a later stage fills in with additional customer
 * types, so the controller never needs a chain of type checks.
 *
 * Satisfaction is encapsulated: it can only change through the clamping mutators here.
 */
public abstract class Customer {
    private final int id;
    private final String name;
    private final int arrivalTurn;

    private int satisfaction = 100;
    private CustomerState state = CustomerState.WAITING;

    private int seatedTurn = -1;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;

    private Table table;
    private Order order;

    protected Customer(int id, String name, int arrivalTurn) {
        this.id = id;
        this.name = name;
        this.arrivalTurn = arrivalTurn;
    }

    // ------------------------------------------------------------------
    // Polymorphic behaviour
    // ------------------------------------------------------------------

    public abstract MenuItem chooseItem(Menu menu, Random rng);

    protected abstract int satisfactionLossPerTurn();

    /** Documented Stage 1 order rule: uniformly chosen from the menu with the seeded source. */
    protected MenuItem pickUniform(Menu menu, Random rng) {
        List<MenuItem> pool = menu.all();
        return pool.get(rng.nextInt(pool.size()));
    }

    // ------------------------------------------------------------------
    // Satisfaction (encapsulated, clamped to 0..100)
    // ------------------------------------------------------------------

    public boolean loseSatisfaction() {
        adjustSatisfaction(-satisfactionLossPerTurn());
        return satisfaction <= 0;
    }

    public void recoverSatisfaction(int amount) {
        adjustSatisfaction(amount);
    }

    public void adjustSatisfaction(int delta) {
        satisfaction = Math.max(0, Math.min(100, satisfaction + delta));
    }

    // ------------------------------------------------------------------
    // State and associations
    // ------------------------------------------------------------------

    public boolean isWaitingForService() {
        return state == CustomerState.WAITING || state == CustomerState.SEATED;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerState getState() {
        return state;
    }

    public void setState(CustomerState state) {
        this.state = state;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getSeatedTurn() {
        return seatedTurn;
    }

    public void setSeatedTurn(int seatedTurn) {
        this.seatedTurn = seatedTurn;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public void setServedTurn(int servedTurn) {
        this.servedTurn = servedTurn;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    public void setReadyToPayTurn(int readyToPayTurn) {
        this.readyToPayTurn = readyToPayTurn;
    }

    public String describe() {
        return name;
    }

    @Override
    public String toString() {
        return describe();
    }
}
