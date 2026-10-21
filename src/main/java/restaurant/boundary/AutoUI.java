package restaurant.boundary;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import restaurant.control.ChefCommand;
import restaurant.control.TurnSummary;
import restaurant.control.WaiterCommand;
import restaurant.domain.ChefAction;
import restaurant.domain.Customer;
import restaurant.domain.CustomerState;
import restaurant.domain.Money;
import restaurant.domain.Order;
import restaurant.domain.OrderStatus;
import restaurant.domain.Restaurant;
import restaurant.domain.WaiterAction;

/**
 * A head-less UI that plays by a fixed, documented policy. It exists so the game can be
 * run reproducibly for the "one victory run and one defeat run" evidence, and it shows
 * the value of the {@link GameUI} abstraction: the same controller runs unchanged.
 *
 * smart = true  -> a sensible "always help the least satisfied customer first" policy.
 * smart = false -> the manager never issues a task, so everyone eventually leaves.
 */
public class AutoUI implements GameUI {
    private final boolean smart;
    private final PrintStream out;

    public AutoUI(boolean smart) {
        this.smart = smart;
        this.out = System.out;
    }

    @Override
    public WaiterCommand chooseWaiterTask(Restaurant restaurant) {
        if (!smart) {
            return WaiterCommand.waitTask();
        }
        // 1. Serve the READY order whose customer is closest to leaving.
        List<Order> ready = restaurant.allOrders().stream().filter(Order::isReady).toList();
        if (!ready.isEmpty()) {
            Order order = ready.stream()
                    .min(Comparator.comparingInt(o -> o.getCustomer().getSatisfaction()))
                    .orElseThrow();
            return new WaiterCommand(WaiterAction.SERVE, order.getId());
        }
        // 2. Take an order from the most urgent seated customer without one.
        List<Customer> seatedNoOrder = restaurant.allCustomers().stream()
                .filter(c -> c.getState() == CustomerState.SEATED && c.getOrder() == null)
                .toList();
        if (!seatedNoOrder.isEmpty()) {
            Customer customer = seatedNoOrder.stream()
                    .min(Comparator.comparingInt(Customer::getSatisfaction))
                    .orElseThrow();
            return new WaiterCommand(WaiterAction.TAKE_ORDER, customer.getId());
        }
        // 3. Seat the most urgent waiting customer when a table is free.
        List<Customer> queue = restaurant.getWaiting().asList();
        if (!queue.isEmpty() && restaurant.hasFreeTable()) {
            Customer customer = queue.stream()
                    .min(Comparator.comparingInt(Customer::getSatisfaction))
                    .orElseThrow();
            return new WaiterCommand(WaiterAction.SEAT, customer.getId());
        }
        return WaiterCommand.waitTask();
    }

    @Override
    public ChefCommand chooseChefTask(Restaurant restaurant) {
        if (!smart) {
            return ChefCommand.waitTask();
        }
        List<Order> pending = restaurant.allOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .toList();
        if (pending.isEmpty()) {
            return ChefCommand.waitTask();
        }
        Order order = pending.stream()
                .min(Comparator.comparingInt(o -> o.getCustomer().getSatisfaction()))
                .orElseThrow();
        return new ChefCommand(ChefAction.ADVANCE, order.getId());
    }

    @Override
    public void showStartOfTurn(Restaurant restaurant) {
        long occupied = restaurant.getTables().stream().filter(t -> !t.isFree()).count();
        out.printf("Turn %2d | revenue %-8s | paid %d | waiting %d | tables %d/%d%n",
                restaurant.getTurn(),
                Money.format(restaurant.getRevenueCents()),
                restaurant.getPaidCount(),
                restaurant.getWaiting().size(),
                occupied,
                restaurant.getTables().size());
    }

    @Override
    public void showTurnResult(TurnSummary summary) {
        out.printf("   -> end of turn %d: served=%d paid=%d unhappy=%d revenue=%s avg=%s%n",
                summary.turnNo(), summary.servedCount(), summary.paidCount(),
                summary.unhappyDepartures(), Money.format(summary.revenueCents()),
                summary.averagePaidSatisfaction().isPresent()
                        ? String.format("%.1f", summary.averagePaidSatisfaction().getAsDouble())
                        : "N/A");
    }

    @Override
    public void showMessage(String message) {
        out.println("   . " + message);
    }

    @Override
    public void showError(String message) {
        out.println("   [!] " + message);
    }

    @Override
    public void showFinal(boolean victory, TurnSummary summary) {
        out.println();
        out.println("================================================");
        out.println(victory ? "  AUTO RUN RESULT: VICTORY" : "  AUTO RUN RESULT: DEFEAT");
        out.printf("  paid=%d  revenue=%s  avg=%s  unhappy=%d%n",
                summary.paidCount(), Money.format(summary.revenueCents()),
                summary.averagePaidSatisfaction().isPresent()
                        ? String.format("%.2f", summary.averagePaidSatisfaction().getAsDouble())
                        : "N/A",
                summary.unhappyDepartures());
        out.println("================================================");
    }
}
