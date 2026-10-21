package restaurant.domain;

/** A single dish on the menu. */
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int prepUnits;

    public MenuItem(String name, int priceCents, int prepUnits) {
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPrepUnits() {
        return prepUnits;
    }

    public String describe() {
        return String.format("%s (%s, %d unit%s)", name, Money.format(priceCents), prepUnits,
                prepUnits == 1 ? "" : "s");
    }

    @Override
    public String toString() {
        return describe();
    }
}
