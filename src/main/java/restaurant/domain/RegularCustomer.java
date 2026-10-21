package restaurant.domain;

import java.util.Random;

/** Ordinary customer: loses 8 satisfaction per waiting turn and pays full price. */
public class RegularCustomer extends Customer {

    public RegularCustomer(int id, String name, int arrivalTurn) {
        super(id, name, arrivalTurn);
    }

    @Override
    public MenuItem chooseItem(Menu menu, Random rng) {
        return pickUniform(menu, rng);
    }

    @Override
    protected int satisfactionLossPerTurn() {
        return 8;
    }
}
