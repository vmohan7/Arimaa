package montecarlo;

import game_phase.GamePhase;

/**
 * An abstract superclass for all "combiners."
 * A combiner outputs a (linear?) combination of the three components
 * computed by Fairy's evaluation function.
 * As a stylistic choice, each implementation of an AbstractCombiner
 * will live within its corresponding *Agent class.
 * @author Neema, Vivek
 */
public abstract class AbstractCombiner {
	protected GamePhase phase;
	
	public AbstractCombiner(GamePhase whichPhase) {
		phase = whichPhase;
	}
	
	/** A setter so that you don't need to create a new Combiner each time the phase changes. */
	public void setGamePhase(GamePhase whichPhase) {
		phase = whichPhase;
	}
	
	public abstract double combineScore(double materialValue, double trapValue, double rabbitValue);
}
