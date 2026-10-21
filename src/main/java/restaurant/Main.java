package restaurant;

import java.util.List;
import java.util.Random;
import restaurant.boundary.AutoUI;
import restaurant.boundary.ConsoleUI;
import restaurant.boundary.GameUI;
import restaurant.control.ArrivalSchedule;
import restaurant.control.GameController;
import restaurant.control.GameResult;
import restaurant.domain.Menu;
import restaurant.domain.MenuItem;
import restaurant.domain.Restaurant;

/**
 * Entry point.
 *
 *   java -cp out restaurant.Main                 play interactively (default seed)
 *   java -cp out restaurant.Main &lt;seed&gt;          play interactively with a fixed seed
 *   java -cp out restaurant.Main --auto [seed]   run the scripted winning policy
 *   java -cp out restaurant.Main --auto-wait     run the scripted losing policy (defeat)
 */
public final class Main {
    private static final long DEFAULT_SEED = 20260101L;

    private Main() {
    }

    public static void main(String[] args) {
        boolean auto = false;
        boolean smart = true;
        long seed = DEFAULT_SEED;

        for (String arg : args) {
            if (arg.equals("--auto")) {
                auto = true;
            } else if (arg.equals("--auto-wait")) {
                auto = true;
                smart = false;
            } else {
                try {
                    seed = Long.parseLong(arg);
                } catch (NumberFormatException e) {
                    System.err.println("Ignoring unrecognised argument: " + arg);
                }
            }
        }

        Menu menu = buildMenu();
        Restaurant restaurant = new Restaurant(menu);
        ArrivalSchedule schedule = new ArrivalSchedule();
        Random rng = new Random(seed);
        GameUI ui = auto ? new AutoUI(smart) : new ConsoleUI();

        System.out.println("Restaurant Manager (Stage 1) - " + (auto ? "auto run" : "interactive")
                + " (seed " + seed + ")");
        System.out.println("Targets: >= 4 paid customers, >= $45.00 revenue, average satisfaction >= 60.");

        GameController controller = new GameController(restaurant, ui, schedule, rng);
        GameResult result;
        try {
            result = controller.run();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("\nInput ended - game stopped before turn 18.");
            System.exit(2);
            return;
        }

        // Non-zero exit code on defeat so scripted runs are easy to check.
        if (!result.isVictory()) {
            System.exit(1);
        }
    }

    /** The Stage 1 menu: the three single dishes. */
    public static Menu buildMenu() {
        return new Menu(List.of(
                new MenuItem("Salad", 900, 1),
                new MenuItem("Burger", 1500, 1),
                new MenuItem("Pasta", 1800, 2)));
    }
}
