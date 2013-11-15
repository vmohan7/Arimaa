package utilities.helper_classes;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

public class ArimaaState {
	private GameState prev, curr;
	private ArimaaMove move;
	
	/** 
	 * <b>A struct with 3 fields:</b> 
	 *  <br>1. previous game state,
	 *  <br>2. current game state,
	 *  <br>3. the move from previous to current
	 * */
	public ArimaaState(GameState prev, GameState curr, ArimaaMove move) {
		this.prev = prev;
		this.curr = curr;
		this.move = move;
	}
	
	/** Copy constructor for an ArimaaState */
	public ArimaaState(ArimaaState toCopy) {
		this.prev = toCopy.getPrev();
		this.curr = toCopy.getCurr();
		this.move = toCopy.getMove();
	}
	
	/** Constructor for initializing an ArimaaState at the beginning of a game. */
	public ArimaaState(GameState startOfGame) {
		this(null, startOfGame, null);
	}
	
	/** 
	 * Updates the fields of an ArimaaState given a move to be played.
	 * */
	public void update(ArimaaMove nextMove) {
		prev = curr;
		
		GameState next = new GameState();
		next.playFullClear(nextMove, curr);
		curr = next;
		move = nextMove;
	}

	public GameState getPrev() { return prev; }
	public GameState getCurr() { return curr; }
	public ArimaaMove getMove() { return move; }
}
