package utilities;

import java.util.ArrayList;
import java.util.Arrays;

import utilities.helper_classes.*;
import arimaa3.ArimaaMove;
import arimaa3.GameState;

public class GameParser {
	
	private ArimaaState arimaaState;
	private String[] moveList;
	private int count;
	
	//TODO: check if "takeback" is handled as a move...and then handle it
	
	public GameParser(GameInfo myGameInfo) {
		initMoveList(myGameInfo);
		initArimaaState(myGameInfo);
		count = -1; //after client gets the start board, count will be 0
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
	/* arimaaState is always one step ahead of the client. That is,
	 * the a copy of the version of arimaaState stored in this class
	 * is returned, and then arimaaState is incremented to the next move.
	 * If this is the last move, arimaaState requires no update, and is
	 * returned as is.
	 * NOTE: count is initialized to -1, so after count++, count
	 *    	 properly indexes into moveList */
	public ArimaaState getNextGameState() {
		if (!hasNextGameState()) return null;
		
		ArimaaState tempAS = new ArimaaState(arimaaState);
		count++; 
		
		if (count != moveList.length) //if we are on the last move, no update to perform
			arimaaState.update(new ArimaaMove(moveList[count]));
		
		return tempAS;
	}
	
	
	private void initMoveList(GameInfo gi) {
		ArrayList<String> tempMoveList = new ArrayList<String>();
		moveList = gi.getMoveList().split("\\r?\\n"); //split on newlines
		
		for (int move = 0; move < moveList.length; move++) {
			if (moveList[move].indexOf("takeback") == -1)
				 //split (e.g. "2w Re2n Re3n Re4n Re5n") into two strings, take the second
				tempMoveList.add(moveList[move].split(" ", 2)[1]); //if this crashes, maybe it's on the last "empty move" signifying the end of the game
			else 
				/* if the move is "takeback", that means the previous move was 
				 * replaced with the one about to be considered */
				tempMoveList.remove(tempMoveList.size() - 1);
		}
		
		//remove the very last "move", which is generally an empty move signifying end of game
		tempMoveList.remove(tempMoveList.size() - 1);
		
		moveList = tempMoveList.toArray(new String[0]); 
				//Arrays.copyOf(tempMoveList.toArray(), tempMoveList.size(), String[].class);
	}
	
	private void initArimaaState(GameInfo gi) {
		String white = gi.getWhiteStartState();
		String black = gi.getBlackStartState();
		
		GameState gs = new GameState(white, black);
		arimaaState = new ArimaaState(gs); //no previous games, no moves
	}
	
}