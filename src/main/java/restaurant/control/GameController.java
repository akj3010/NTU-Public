package restaurant.control;

import java.util.Random;
import restaurant.boundary.GameUI;
import restaurant.domain.Chef;
import restaurant.domain.ChefAction;
import restaurant.domain.Customer;
import restaurant.domain.CustomerState;
import restaurant.domain.Money;
import restaurant.domain.Order;
import restaurant.domain.OrderStatus;
import restaurant.domain.Outcome;
import restaurant.domain.Restaurant;
import restaurant.domain.Table;
import restaurant.domain.Waiter;
import restaurant.domain.WaiterAction;

/**
 * Runs the game. The controller owns ONLY the sequencing of the six turn steps from
 * Section 3.2 and the "at most one task per staff member" enforcement; every game rule
 * lives in the domain objects. The UI is used through the {@link GameUI} abstraction.
 */
public class GameController {
    private static final int MAX_TURNS = 18;
    private static final int TARGET_PAID_CUSTOMERS = 4;
    private static final int TARGET_REVENUE_CENTS = 4500;      // $45.00
    private static final int TARGET_AVERAGE_SATISFACTION = 60;

    private final Restaurant restaurant;
    private final GameUI ui;
    private final ArrivalSchedule schedule;
    private final Random rng;

    /** The chef's decision is taken during step 2 and applied during step 3. */
    private ChefCommand pendingChefTask;

    public GameController(Restaurant restaurant, GameUI ui, ArrivalSchedule schedule, Random rng) {
        this.restaurant = restaurant;
        this.ui = ui;
        this.schedule = schedule;
        this.rng = rng;
    }

    public GameResult run() {
        for (int turn = 1; turn <= MAX_TURNS; turn++) {
            restaurant.setTurn(turn);
            startOfTurn();
            stepArrivals(turn);
            stepManagerChoices(turn);
            stepKitchenAndServing();
            stepEatingAndPayment(turn);
            stepWaitingAndAbandonment();
            ui.showTurnResult(buildSummary());
        }
        GameResult result = checkObjective();
        ui.showFinal(result.isVictory(), result.summary());
        return result;
    }

    // ------------------------------------------------------------------
    // Start of turn: Section 3.1 display
    // ------------------------------------------------------------------

    private void startOfTurn() {
        restaurant.resetStaffTurn();
        ui.showStartOfTurn(restaurant);
    }

    // ------------------------------------------------------------------
    // Step 1: arrivals
    // ------------------------------------------------------------------

    private void stepArrivals(int turn) {
        for (Customer customer : schedule.customersFor(turn, rng)) {
            restaurant.addCustomer(customer);
            ui.showMessage(String.format("Turn %d: %s arrives (satisfaction 100).", turn, customer.describe()));
        }
    }

    // ------------------------------------------------------------------
    // Step 2: manager choices (waiter acts immediately; chef decision stored for step 3)
    // ------------------------------------------------------------------

    private void stepManagerChoices(int turn) {
        WaiterCommand waiterTask;
        while (true) {
            waiterTask = ui.chooseWaiterTask(restaurant);
            String error = validateWaiterTask(waiterTask);
            if (error == null) {
                break;
            }
            ui.showError(error);   // rejected without spending the task - the manager tries again
        }
        applyWaiterTask(waiterTask, turn);

        ChefCommand chefTask;
        while (true) {
            chefTask = ui.chooseChefTask(restaurant);   // may now target the order the waiter just took
            String error = validateChefTask(chefTask);
            if (error == null) {
                break;
            }
            ui.showError(error);
        }
        this.pendingChefTask = chefTask;
    }

    private String validateWaiterTask(WaiterCommand command) {
        return switch (command.action()) {
            case WAIT -> null;
            case SEAT -> {
                if (!restaurant.hasFreeTable()) {
                    yield "Cannot seat: both tables are occupied.";
                }
                if (findWaitingCustomer(command.targetId()) == null) {
                    yield "That customer is not in the waiting queue.";
                }
                yield null;
            }
            case TAKE_ORDER -> {
                Customer customer = customerOrNull(command.targetId());
                if (customer == null) {
                    yield "Unknown customer.";
                }
                if (customer.getState() != CustomerState.SEATED) {
                    yield customer.getName() + " is not seated yet.";
                }
                if (customer.getOrder() != null) {
                    yield customer.getName() + " has already placed an order.";
                }
                yield null;
            }
            case SERVE -> {
                Order order = orderOrNull(command.targetId());
                if (order == null) {
                    yield "Unknown order.";
                }
                if (!order.isReady()) {
                    yield "Order #" + order.getId() + " is not READY yet (status " + order.getStatus()
                            + "). A dish finished this turn can only be served next turn.";
                }
                yield null;
            }
        };
    }

    private void applyWaiterTask(WaiterCommand command, int turn) {
        Waiter waiter = restaurant.getWaiter();
        switch (command.action()) {
            case WAIT:
                return;
            case SEAT: {
                Customer customer = restaurant.findCustomer(command.targetId());
                Table table = restaurant.firstFreeTable();
                waiter.seat(customer, table, turn);
                restaurant.removeFromWaiting(customer);
                waiter.markActed();
                ui.showMessage(customer.getName() + " is seated at table " + table.getId() + ".");
                return;
            }
            case TAKE_ORDER: {
                Customer customer = restaurant.findCustomer(command.targetId());
                Order order = waiter.takeOrder(customer, restaurant.getMenu(), rng, restaurant);
                waiter.markActed();
                ui.showMessage(customer.getName() + " orders " + order.getItem().describe()
                        + " - bill " + Money.format(order.billCents()) + ".");
                return;
            }
            case SERVE: {
                Order order = restaurant.findOrder(command.targetId());
                waiter.serve(order, turn);
                restaurant.recordServing();
                waiter.markActed();
                ui.showMessage("Served " + order.getItem().getName() + " to " + order.getCustomer().getName() + ".");
                return;
            }
            default:
                return;
        }
    }

    private String validateChefTask(ChefCommand command) {
        if (command.action() == ChefAction.WAIT) {
            return null;
        }
        Order order = orderOrNull(command.orderId());
        if (order == null) {
            return "Unknown order.";
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            return "Order #" + order.getId() + " cannot be advanced (status " + order.getStatus() + ").";
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Step 3: kitchen and serving (the waiter already acted; now the chef advances)
    // ------------------------------------------------------------------

    private void stepKitchenAndServing() {
        ChefCommand command = pendingChefTask;
        pendingChefTask = null;
        if (command == null || command.action() == ChefAction.WAIT) {
            return;
        }
        Order order = restaurant.findOrder(command.orderId());
        Chef chef = restaurant.getChef();
        chef.advance(order);
        chef.markActed();
        if (order.isReady()) {
            ui.showMessage("Order #" + order.getId() + " (" + order.getItem().getName()
                    + ") is READY - it can be served from next turn.");
        } else {
            ui.showMessage("Chef works on order #" + order.getId() + " (" + order.getStatus()
                    + ", " + order.remainingUnits() + " unit(s) to go).");
        }
    }

    // ------------------------------------------------------------------
    // Step 4: eating and payment
    // ------------------------------------------------------------------

    private void stepEatingAndPayment(int turn) {
        // A customer served on an earlier turn finishes eating and becomes READY_TO_PAY.
        for (Customer customer : restaurant.allCustomers()) {
            if (customer.getState() == CustomerState.EATING && customer.getServedTurn() < turn) {
                customer.setState(CustomerState.READY_TO_PAY);
                customer.setReadyToPayTurn(turn);
            }
        }
        // The cashier handles at most one READY_TO_PAY customer, oldest first, that was
        // already ready before this turn.
        Customer payer = restaurant.oldestReadyToPay(turn);
        if (payer != null) {
            int amount = restaurant.getCashier().collectPayment(payer, restaurant);
            ui.showMessage(String.format("Cashier collects %s from %s (satisfaction %d recorded).",
                    Money.format(amount), payer.getName(), payer.getSatisfaction()));
        }
    }

    // ------------------------------------------------------------------
    // Step 5: waiting and abandonment
    // ------------------------------------------------------------------

    private void stepWaitingAndAbandonment() {
        for (Customer customer : restaurant.allCustomers()) {
            if (!customer.isWaitingForService()) {
                continue;   // served, eating or ready to pay customers lose nothing
            }
            boolean reachedZero = customer.loseSatisfaction();
            if (reachedZero) {
                restaurant.removeFromWaiting(customer);
                if (customer.getOrder() != null) {
                    customer.getOrder().cancel();
                }
                Table table = customer.getTable();
                if (table != null) {
                    table.vacate();
                    customer.setTable(null);
                }
                customer.setState(CustomerState.LEFT);
                restaurant.recordUnhappyExit();
                ui.showMessage(customer.getName() + " left without paying (satisfaction reached 0).");
            }
        }
    }

    // ------------------------------------------------------------------
    // Step 6: result
    // ------------------------------------------------------------------

    private TurnSummary buildSummary() {
        return new TurnSummary(
                restaurant.getTurn(),
                restaurant.getServedCount(),
                restaurant.getPaidCount(),
                restaurant.getUnhappyDepartures(),
                restaurant.getRevenueCents(),
                restaurant.averagePaidSatisfaction(),
                restaurant.getTurn());
    }

    private GameResult checkObjective() {
        boolean enoughPaid = restaurant.getPaidCount() >= TARGET_PAID_CUSTOMERS;
        boolean enoughRevenue = restaurant.getRevenueCents() >= TARGET_REVENUE_CENTS;
        boolean enoughSatisfaction = restaurant.averagePaidSatisfaction().orElse(0.0) >= TARGET_AVERAGE_SATISFACTION;
        boolean victory = enoughPaid && enoughRevenue && enoughSatisfaction;
        return new GameResult(victory ? Outcome.VICTORY : Outcome.DEFEAT, buildSummary());
    }

    // ------------------------------------------------------------------
    // Lookup helpers
    // ------------------------------------------------------------------

    private Customer customerOrNull(int id) {
        return restaurant.allCustomers().stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private Order orderOrNull(int id) {
        return restaurant.allOrders().stream().filter(o -> o.getId() == id).findFirst().orElse(null);
    }

    private Customer findWaitingCustomer(int id) {
        return restaurant.getWaiting().asList().stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }
}
