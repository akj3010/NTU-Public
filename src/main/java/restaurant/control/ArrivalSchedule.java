package restaurant.control;

import java.util.List;
import java.util.Random;
import restaurant.domain.Customer;
import restaurant.domain.RegularCustomer;

/**
 * Fixed, reproducible arrival plan for the base setting: one regular customer at the
 * start of turns 1, 4, 7, 10, 13 and 16 (six in total).
 */
public class ArrivalSchedule {
    private final int[] turns = {1, 4, 7, 10, 13, 16};
    private int nextId = 1;

    public int[] arrivalTurns() {
        return turns.clone();
    }

    /** Returns the customers arriving on this turn (empty when nobody is scheduled). */
    public List<Customer> customersFor(int turn, Random rng) {
        if (!isArrivalTurn(turn)) {
            return List.of();
        }
        int id = nextId++;
        return List.of(new RegularCustomer(id, "Guest #" + id, turn));
    }

    private boolean isArrivalTurn(int turn) {
        for (int t : turns) {
            if (t == turn) {
                return true;
            }
        }
        return false;
    }
}
