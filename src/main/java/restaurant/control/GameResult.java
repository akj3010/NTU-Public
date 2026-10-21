package restaurant.control;

import restaurant.domain.Outcome;

/** The final result of a full game: the outcome and the closing summary. */
public record GameResult(Outcome outcome, TurnSummary summary) {

    public boolean isVictory() {
        return outcome == Outcome.VICTORY;
    }
}
