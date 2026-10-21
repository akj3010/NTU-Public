package restaurant.control;

import java.util.OptionalDouble;

/** The end-of-turn report (Section 3.5). */
public record TurnSummary(int turnNo,
                          int servedCount,
                          int paidCount,
                          int unhappyDepartures,
                          int revenueCents,
                          OptionalDouble averagePaidSatisfaction,
                          int turnsUsed) {
}
