package naive_bayes;

import java.util.ArrayList;

import arimaa3.ArimaaMove;
import arimaa3.GameState;
import naive_bayes.helper_classes.*;

public class GameParser {
	
	private ArimaaState arimaaState;
	private String[] moveList;
	private int count;
	
	//TODO: check if "takeback" is handled as a move...and then handle it
	
	public GameParser(GameInfo myGameInfo) {
		initMoveList(myGameInfo);
		initArimaaState(myGameInfo);
		count = 0;
	}
	
	public boolean hasNextGameState() {
		return count < moveList.length;
	}
	
	/**
	 *  @return An <b>ArimaaState</b> containing 3 fields:
	 *    <br>1 - previous board
	 *    <br>2 - current board, resulting from the move applied to previous board
	 *    <br>3 - the move itself
	 *    <br><i> or </i><b>null</b><i> if there are no more game states 
	 *    			(i.e. the game is over)</i>
	 */
	public ArimaaState getNextGameState() {
		if (!hasNextGameState()) return null;
		
		ArimaaState tempAS = new ArimaaState(arimaaState);
		arimaaState.update(new ArimaaMove(moveList[count]));
		count++;
		
		return tempAS;
	}
	
	
	private void initMoveList(GameInfo gi) {
		ArrayList<String> tempMoveList = new ArrayList<String>();
		moveList = gi.getMoveList().split("\\r?\\n"); //split on newlines
		
		for (int move = 0; move < moveList.length; move++) {
			if (moveList[move].indexOf("takeback") != -1)
				tempMoveList.add(moveList[move]);
			else 
				/* if the move is "takeback", that means the previous move was 
				 * replaced with the one about to be considered */
				tempMoveList.remove(tempMoveList.size() - 1);
		}
		
		//remove the very last "move", which is generally an empty move signifying end of game
		tempMoveList.remove(tempMoveList.size() - 1);
		
		moveList = (String[])tempMoveList.toArray();
	}
	
	private void initArimaaState(GameInfo gi) {
		String white = gi.getWhiteStartState();
		String black = gi.getBlackStartState();
		
		GameState gs = new GameState(white, black);
		arimaaState = new ArimaaState(gs); //no previous games, no moves
	}
	
}