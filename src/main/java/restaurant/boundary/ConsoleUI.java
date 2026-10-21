package restaurant.boundary;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.function.Function;
import restaurant.control.ChefCommand;
import restaurant.control.TurnSummary;
import restaurant.control.WaiterCommand;
import restaurant.domain.ChefAction;
import restaurant.domain.Customer;
import restaurant.domain.CustomerState;
import restaurant.domain.Money;
import restaurant.domain.Order;
import restaurant.domain.Restaurant;
import restaurant.domain.Table;
import restaurant.domain.WaiterAction;

/**
 * The command-line interface. It ONLY collects the manager's decisions and displays the
 * state - it contains no game rules. Any illegal choice is caught by the controller and
 * explained back through {@link #showError(String)}.
 */
public class ConsoleUI implements GameUI {
    private final Scanner in;
    private final PrintStream out;

    public ConsoleUI() {
        this(System.in, System.out);
    }

    public ConsoleUI(InputStream in, PrintStream out) {
        this.in = new Scanner(in);
        this.out = out;
    }

    // ------------------------------------------------------------------
    // Display
    // ------------------------------------------------------------------

    @Override
    public void showMessage(String message) {
        out.println("  " + message);
    }

    @Override
    public void showError(String message) {
        out.println("  [!] " + message);
    }

    @Override
    public void showStartOfTurn(Restaurant restaurant) {
        out.println();
        out.println("============================================================");
        out.printf("TURN %d / 18%n", restaurant.getTurn());
        out.println("------------------------------------------------------------");
        out.printf("Revenue: %-9s Paid customers: %-3d Unhappy exits: %d%n",
                Money.format(restaurant.getRevenueCents()),
                restaurant.getPaidCount(),
                restaurant.getUnhappyDepartures());
        out.printf("Average satisfaction (paid): %s%n", formatAverage(restaurant.averagePaidSatisfaction()));
        out.println();

        out.println("Waiting queue:");
        List<Customer> waiting = restaurant.getWaiting().asList();
        if (waiting.isEmpty()) {
            out.println("  (empty)");
        } else {
            for (Customer customer : waiting) {
                out.printf("  #%d %-10s satisfaction=%d%n",
                        customer.getId(), customer.getName(), customer.getSatisfaction());
            }
        }

        out.println("Tables:");
        for (Table table : restaurant.getTables()) {
            if (table.isFree()) {
                out.printf("  Table %d: free%n", table.getId());
            } else {
                Customer occupant = table.getOccupant();
                out.printf("  Table %d: %-10s [%s] satisfaction=%d%n",
                        table.getId(), occupant.getName(), occupant.getState(), occupant.getSatisfaction());
            }
        }

        out.println("Customers:");
        for (Customer customer : restaurant.allCustomers()) {
            String order = (customer.getOrder() == null) ? "-" : customer.getOrder().describe();
            out.printf("  #%-2d %-10s %-12s satisfaction=%-3d order: %s%n",
                    customer.getId(), customer.getName(), customer.getState(),
                    customer.getSatisfaction(), order);
        }

        out.println("Staff tasks available:");
        out.printf("  Waiter : %s%n", restaurant.getWaiter().canAct() ? "ready" : "already acted");
        out.printf("  Chef   : %s%n", restaurant.getChef().canAct() ? "ready" : "already acted");
        out.println("============================================================");
    }

    @Override
    public void showTurnResult(TurnSummary summary) {
        out.println("------------------------------------------------------------");
        out.printf("End of turn %d:%n", summary.turnNo());
        out.printf("  Served: %d   Paid: %d   Unhappy exits: %d%n",
                summary.servedCount(), summary.paidCount(), summary.unhappyDepartures());
        out.printf("  Revenue: %s%n", Money.format(summary.revenueCents()));
        out.printf("  Average satisfaction of paid customers: %s%n",
                formatAverage(summary.averagePaidSatisfaction()));
        out.printf("  Turns used: %d%n", summary.turnsUsed());
        out.println("------------------------------------------------------------");
    }

    @Override
    public void showFinal(boolean victory, TurnSummary summary) {
        out.println();
        out.println("############################################################");
        out.println(victory ? "  RESULT: VICTORY" : "  RESULT: DEFEAT");
        out.println("  Targets: >= 4 paid, >= $45.00 revenue, average satisfaction >= 60");
        out.printf("  Achieved: %d paid, %s revenue, average %s%n",
                summary.paidCount(), Money.format(summary.revenueCents()),
                formatAverage(summary.averagePaidSatisfaction()));
        out.println("############################################################");
    }

    // ------------------------------------------------------------------
    // Manager input
    // ------------------------------------------------------------------

    @Override
    public WaiterCommand chooseWaiterTask(Restaurant restaurant) {
        while (true) {
            out.println();
            out.println("Waiter - choose ONE task:");
            out.println("  [0] Wait");
            out.println("  [1] Seat a queued customer");
            out.println("  [2] Take an order from a seated customer");
            out.println("  [3] Serve a READY order");
            int choice = readInt("Choice: ");
            switch (choice) {
                case 0:
                    return WaiterCommand.waitTask();
                case 1: {
                    List<Customer> queue = restaurant.getWaiting().asList();
                    if (queue.isEmpty()) {
                        showError("No customers are waiting to be seated.");
                        continue;
                    }
                    printIndexed(queue, c -> String.format("#%d %s (satisfaction %d)",
                            c.getId(), c.getName(), c.getSatisfaction()));
                    int index = readInt("Seat which customer: ");
                    if (index < 1 || index > queue.size()) {
                        showError("Invalid selection.");
                        continue;
                    }
                    return new WaiterCommand(WaiterAction.SEAT, queue.get(index - 1).getId());
                }
                case 2: {
                    List<Customer> seated = restaurant.allCustomers().stream()
                            .filter(c -> c.getState() == CustomerState.SEATED)
                            .toList();
                    if (seated.isEmpty()) {
                        showError("No seated customers to take an order from.");
                        continue;
                    }
                    printIndexed(seated, c -> String.format("#%d %s (satisfaction %d)%s",
                            c.getId(), c.getName(), c.getSatisfaction(),
                            c.getOrder() == null ? "" : " [already ordered]"));
                    int index = readInt("Take the order of which customer: ");
                    if (index < 1 || index > seated.size()) {
                        showError("Invalid selection.");
                        continue;
                    }
                    return new WaiterCommand(WaiterAction.TAKE_ORDER, seated.get(index - 1).getId());
                }
                case 3: {
                    List<Order> orders = restaurant.openOrders();
                    if (orders.isEmpty()) {
                        showError("There are no open orders.");
                        continue;
                    }
                    printIndexed(orders, Order::describe);
                    int index = readInt("Serve which order: ");
                    if (index < 1 || index > orders.size()) {
                        showError("Invalid selection.");
                        continue;
                    }
                    return new WaiterCommand(WaiterAction.SERVE, orders.get(index - 1).getId());
                }
                default:
                    showError("Enter 0, 1, 2 or 3.");
            }
        }
    }

    @Override
    public ChefCommand chooseChefTask(Restaurant restaurant) {
        while (true) {
            out.println();
            out.println("Chef - choose ONE task:");
            out.println("  [0] Wait");
            out.println("  [1] Advance an order by one preparation unit");
            int choice = readInt("Choice: ");
            switch (choice) {
                case 0:
                    return ChefCommand.waitTask();
                case 1: {
                    List<Order> orders = restaurant.openOrders();
                    if (orders.isEmpty()) {
                        showError("There are no orders to work on.");
                        continue;
                    }
                    printIndexed(orders, Order::describe);
                    int index = readInt("Advance which order: ");
                    if (index < 1 || index > orders.size()) {
                        showError("Invalid selection.");
                        continue;
                    }
                    return new ChefCommand(ChefAction.ADVANCE, orders.get(index - 1).getId());
                }
                default:
                    showError("Enter 0 or 1.");
            }
        }
    }

    // ------------------------------------------------------------------
    // Input helpers (robust against non-numeric and out-of-range input)
    // ------------------------------------------------------------------

    private String readLine(String prompt) {
        out.print(prompt);
        if (!in.hasNextLine()) {
            throw new NoSuchElementException("Input stream closed.");
        }
        return in.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                showError("Please enter a whole number.");
            }
        }
    }

    private <T> void printIndexed(List<T> list, Function<T, String> formatter) {
        for (int i = 0; i < list.size(); i++) {
            out.printf("  [%d] %s%n", i + 1, formatter.apply(list.get(i)));
        }
    }

    private String formatAverage(OptionalDouble average) {
        return average.isPresent() ? String.format("%.2f", average.getAsDouble()) : "N/A";
    }
}
