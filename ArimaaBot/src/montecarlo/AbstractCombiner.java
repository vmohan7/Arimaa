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
	
	/** Default combination. */
	public int combineScore(int materialValue, int trapValue, int rabbitValue) {
		return materialValue + trapValue + rabbitValue;
	}
	
	/** Default framed-piece weighting. */
	public int frameValue(int materialValue) {
		return materialValue * 4/5;
	}
	
	/** Default penalty for advanced (cat|dog). */
	public int advancedValue(int catDogValue) {
		return catDogValue * 197 / 200;
	}
	
	/** Default penalty for slightly advanced (cat|dog). */
	public int slightlyAdvancedValue(int catDogValue) {
		return catDogValue * 199 / 200;
	}
}
