package utilities;

import java.sql.ResultSet;
import java.sql.SQLException;

import utilities.helper_classes.GameInfo;

public class GameData {
	private ResultSet filteredGames; 
	private int firstTestGame;
	private Mode myMode;
	
	private static final int RATING_THRESHOLD = 2100;
	// For reference, there are 279K total games, and 5K where both players are rated > 2100. 
	
	private static String myQuery = "SELECT w_state, b_state, movelist FROM " +
			"(SELECT id FROM games WHERE white_rating >= %d " +
			"AND black_rating >= %d ORDER BY RAND() LIMIT %d ) g_ids " +
			"INNER JOIN games ON games.id = g_ids.id"; 
	
	public static enum Mode {
		TRAIN, TEST;
	}
	
	public GameData(int numGames, double trainFraction){
		this.firstTestGame = (int) (trainFraction * numGames) + 1;
		filteredGames = MyDB.executeQuery(String.format(myQuery, RATING_THRESHOLD, RATING_THRESHOLD, numGames));
		setMode(Mode.TRAIN);
	}
	
	public boolean hasNextGame(){
		try {
			if (myMode == Mode.TRAIN && filteredGames.getRow() >= firstTestGame)
				return false;
			else if (myMode == Mode.TEST && filteredGames.isAfterLast())
				return false;
			else return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public GameInfo getNextGame(){
		try {
			
			GameInfo ret = new GameInfo(filteredGames.getString("w_state"),
					filteredGames.getString("b_state"),
					filteredGames.getString("movelist"));
			
			filteredGames.next();
			return ret;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Switching to training mode resets the cursor to the first training game.
	 * @param myMode The mode you want to switch to.
	 */
	public void setMode(Mode myMode){
		this.myMode = myMode; 
		if(myMode == Mode.TRAIN) {
			try {
				filteredGames.first();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
