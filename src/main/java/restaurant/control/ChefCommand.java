package restaurant.control;

import restaurant.domain.ChefAction;

/**
 * One manager decision for the Chef. orderId identifies the order to advance (ignored
 * when the action is WAIT).
 */
public record ChefCommand(ChefAction action, int orderId) {

    public static ChefCommand waitTask() {
        return new ChefCommand(ChefAction.WAIT, -1);
    }
}
