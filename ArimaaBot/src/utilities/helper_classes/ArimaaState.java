package utilities.helper_classes;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

/** 
 * <b>A struct with 6 fields:</b> 
 *  <br>1. game state before previous game state
 *  <br>2. previous game state,
 *  <br>3. current game state,
 *  <br>4. the move before the previous move played to get to previous position
 *  <br>5. the previous move from previous to current position
 *  <br>6. the (expert) move from current to the next (expert) position 
 * */
public class ArimaaState {
	private GameState prevPrev, prev, curr;
	private ArimaaMove prevPrevMove, prevMove, nextMove;
	
	/**
	 * @param prevPrev game state before prev game state
	 * @param prev previous game state
	 * @param curr current game state
	 * @param prevPrevMove the move before the previous move played to get to previous position
	 * @param prevMove the previous move from previous to current position
	 * @param nextMove the (expert) move from current to the next (expert) position
	 * */
	public ArimaaState(GameState prevPrev, GameState prev, GameState curr, ArimaaMove prevPrevMove, ArimaaMove prevMove, ArimaaMove nextMove) {
		this.prevPrev = prevPrev;
		this.prev = prev;
		this.curr = curr;
		this.nextMove = nextMove;
		this.prevPrevMove = prevPrevMove;
		this.prevMove = prevMove;
	}
	
	/** Copy constructor for an ArimaaState */
	public ArimaaState(ArimaaState toCopy) {
		this.prevPrev = toCopy.getPrevPrev();
		this.prev = toCopy.getPrev();
		this.curr = toCopy.getCurr();
		this.prevPrevMove = toCopy.getPrevPrevMove();
		this.prevMove = toCopy.getPrevMove();
		this.nextMove = toCopy.getNextMove();
	}

	/** Constructor for initializing an ArimaaState at the beginning of a game. */
	public ArimaaState(GameState startOfGame, ArimaaMove nextMove) {
		this(null, null, startOfGame, null, null, nextMove);
	}

	/** 
	 * Updates the fields of an ArimaaState given the move after next move 
	 * to be played. (ArimaaState stores the move after current, and plays the
	 * stored move.)
	 * @param nextNextMove updates ArimaaState's nextMove field once that stored
	 * move is played. 
	 * */
	public void update(ArimaaMove nextNextMove) {
		prevPrev = prev;
		prev = curr;
		prevPrevMove = prevMove;
		prevMove = nextMove;
		
		GameState next = new GameState();
		next.playFullClear(nextMove, curr); 
		curr = next;
		nextMove = nextNextMove;
	}

	public GameState getPrevPrev() { return prevPrev; }
	public GameState getPrev() { return prev; }
	public GameState getCurr() { return curr; }
	public ArimaaMove getPrevPrevMove() { return prevPrevMove; }
	public ArimaaMove getPrevMove() { return prevMove; }
	public ArimaaMove getNextMove() { return nextMove; }
}
