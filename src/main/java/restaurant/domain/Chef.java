package restaurant.domain;

/** The chef advances at most one order by a single preparation unit per turn. */
public class Chef extends Staff {

    public Chef() {
        super("Chef");
    }

    public void advance(Order order) {
        order.advance();
    }
}
