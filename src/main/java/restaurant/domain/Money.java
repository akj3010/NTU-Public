package restaurant.domain;

/** Money is stored everywhere as integer cents (Section 2.2). */
public final class Money {
    private Money() {
    }

    /** Formats cents as a dollar string: 1800 -> "$18.00". */
    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, Math.abs(cents % 100));
    }
}
