package restaurant.domain;

/** The single task the manager may assign to the Chef each turn. */
public enum ChefAction {
    ADVANCE, // advance one order by one preparation unit
    WAIT     // do nothing
}
