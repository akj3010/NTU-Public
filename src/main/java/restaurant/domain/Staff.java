package restaurant.domain;

/**
 * Base class for the restaurant staff. Its real job is the shared rule that every role
 * performs at most one task per turn: the flag, the check and the reset live here once.
 */
public abstract class Staff {
    private final String name;
    private boolean actedThisTurn;

    protected Staff(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** True while this member of staff still has a task available this turn. */
    public boolean canAct() {
        return !actedThisTurn;
    }

    public void markActed() {
        actedThisTurn = true;
    }

    /** Called at the start of every turn to give the member of staff a fresh task. */
    public void beginTurn() {
        actedThisTurn = false;
    }
}
