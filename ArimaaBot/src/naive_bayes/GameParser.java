package naive_bayes;

import java.util.ArrayList;

import naive_bayes.helper_classes.*;

public class GameParser {
	
	private ArimaaState arimaaState;
	String[] moveList;
	
	//TODO: check if "takeback" is handled as a move...and then handle it
	
	public GameParser(GameInfo myGameInfo) {
		ArrayList<String> tempMoveList = new ArrayList<String>();
		moveList = myGameInfo.getMoveList().split("\\r?\\n"); //split on newlines
		
		for (int move = 0; move < moveList.length; move++) {
		}
	}
	
	
	public boolean hasNextGameState() {
		
		
		return false;
	}
	
	/**
	 *  @return An <b>ArimaaState</b> containing 4 fields:
	 *    <br>0 - previous previous board
	 *    <br>1 - previous board
	 *    <br>2 - the move itself
	 *    <br>3 - current board
	 *    <br><i> or </i><b>null</b><i> if there are no more game states 
	 *    			(i.e. the game is over)</i>
	 */
	public ArimaaState getNextGameState() {
		//return null if no more game states
		
		return null;
	}
}