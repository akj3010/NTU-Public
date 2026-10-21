package restaurant.control;

import restaurant.domain.WaiterAction;

/**
 * One manager decision for the Waiter, produced by the UI and validated/dispatched by the
 * controller. targetId is a customer id for SEAT/TAKE_ORDER and an order id for SERVE.
 */
public record WaiterCommand(WaiterAction action, int targetId) {

    public static WaiterCommand waitTask() {
        return new WaiterCommand(WaiterAction.WAIT, -1);
    }
}
