package naive_bayes.helper_classes;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

public class ArimaaState {
	private GameState prev_prev, prev, curr;
	private ArimaaMove move;
	
	public ArimaaState(GameState prev_prev, GameState prev, GameState curr, ArimaaMove move) {
		this.prev_prev = prev_prev;
		this.prev = prev;
		this.curr = curr;
		this.move = move;
	}

	public GameState getPrev_prev() { return prev_prev; }
	public GameState getPrev() { return prev; }
	public GameState getCurr() { return curr; }
	public ArimaaMove getMove() { return move; }
}
