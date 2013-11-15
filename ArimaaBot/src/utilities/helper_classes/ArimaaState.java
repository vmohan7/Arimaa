package utilities.helper_classes;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

/** 
 * <b>A struct with 3 fields:</b> 
 *  <br>1. previous game state,
 *  <br>2. current game state,
 *  <br>3. the (expert) move from current to the next (expert) position 
 * */
public class ArimaaState {
	private GameState prev, curr;
	private ArimaaMove nextMove;
	
	/**
	 * @param prev previous game state
	 * @param curr current game state
	 * @param nextMove the (expert) move from current to the next (expert) position
	 * */
	public ArimaaState(GameState prev, GameState curr, ArimaaMove nextMove) {
		this.prev = prev;
		this.curr = curr;
		this.nextMove = nextMove;
	}
	
	/** Copy constructor for an ArimaaState */
	public ArimaaState(ArimaaState toCopy) {
		this.prev = toCopy.getPrev();
		this.curr = toCopy.getCurr();
		this.nextMove = toCopy.getNextMove();
	}
	
	/** Constructor for initializing an ArimaaState at the beginning of a game. */
	public ArimaaState(GameState startOfGame, ArimaaMove nextMove) {
		this(null, startOfGame, nextMove);
	}
	
	/** 
	 * Updates the fields of an ArimaaState given the move after next move 
	 * to be played. (ArimaaState stores the move after current, and plays the
	 * stored move.)
	 * @param nextNextMove updates ArimaaState's nextMove field once that stored
	 * move is played. 
	 * */
	public void update(ArimaaMove nextNextMove) {
		prev = curr;
		
		GameState next = new GameState();
		next.playFullClear(nextMove, curr); 
		curr = next;
		nextMove = nextNextMove;
	}

	public GameState getPrev() { return prev; }
	public GameState getCurr() { return curr; }
	public ArimaaMove getNextMove() { return nextMove; }
}
