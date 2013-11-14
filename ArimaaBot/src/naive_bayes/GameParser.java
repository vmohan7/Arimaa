package naive_bayes;

import naive_bayes.helper_classes.*;

public class GameParser {
	
	
	public GameParser(GameInfo myGameInfo) {
		
	}
	
	
	public boolean hasNextGameState() {
		
		
		return false;
	}
	
	/* obj[0] -> prev; obj[1] -> move; obj[2] -> curr that results from applying move to prev */
	/**
	 *  @return An <b>array of 3 objects</b>:
	 *    <br>0 - previous board
	 *    <br>1 - the move itself
	 *    <br>2 - current board
	 *    <br><i> or </i><b>null</b><i> if there are no more game states 
	 *    			(i.e. the game is over)</i>
	 */
	public Object[] getNextGameState() {
		//return null if no more game states
		
		return null;
	}
}