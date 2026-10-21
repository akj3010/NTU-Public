package restaurant.boundary;

import restaurant.control.ChefCommand;
import restaurant.control.TurnSummary;
import restaurant.control.WaiterCommand;
import restaurant.domain.Restaurant;

/**
 * The boundary between the game and whoever is playing it. The controller talks only to
 * this abstraction (DIP), so the game can run head-less (AutoUI) for testing or through
 * the console (ConsoleUI) without changing a single rule.
 */
public interface GameUI {

    /** Section 3.1: show the state at the start of a turn. */
    void showStartOfTurn(Restaurant restaurant);

    /** Ask the manager for the waiter's single task this turn. */
    WaiterCommand chooseWaiterTask(Restaurant restaurant);

    /** Ask the manager for the chef's single task this turn. */
    ChefCommand chooseChefTask(Restaurant restaurant);

    /** Section 3.5: report the result of the turn that just finished. */
    void showTurnResult(TurnSummary summary);

    /** A normal informational message. */
    void showMessage(String message);

    /** An explanation of why a manager choice was rejected. */
    void showError(String message);

    /** The final victory or defeat banner. */
    void showFinal(boolean victory, TurnSummary summary);
}
