package utilities;

import java.sql.ResultSet;
import java.sql.SQLException;

import utilities.helper_classes.GameInfo;

public class GameData implements AbstractGameData {
	private ResultSet filteredGames; 
	private int firstTestGame;
	private Mode myMode;
	private int numGames;
	
	public static final boolean USING_EXPERT = true;
	private static int ratingThreshold = 2100;
	
	public static int getRatingThreshold() { return ratingThreshold; }

	public static void setRatingThreshold(int threshold) {
		assert(threshold > 1300);
		ratingThreshold = threshold;
	}
	
	// For reference, there are 279K total games, and 5K where both players are rated > 2100. 
	
	private static final String EXPERT_QUERY = "SELECT games.id, w_state, b_state, movelist FROM " +
			"(SELECT id FROM games WHERE white_rating >= %d " +
			"AND black_rating >= %d ORDER BY RAND() LIMIT %d ) g_ids " +
			"INNER JOIN games ON games.id = g_ids.id"; 
	
	private static final String ANY_QUERY = "SELECT games.id, w_state, b_state, movelist FROM " +
			"(SELECT id FROM games ORDER BY RAND() LIMIT %d ) g_ids " +
			"INNER JOIN games ON games.id = g_ids.id";
	
	private static final String myQuery = USING_EXPERT ? EXPERT_QUERY : ANY_QUERY;
	
	public GameData(int numGames, double trainFraction){
		this.numGames = numGames;
		this.firstTestGame = (int) (trainFraction * numGames) + 1;
		
		if (myQuery == EXPERT_QUERY)
			filteredGames = MyDB.executeQuery(String.format(myQuery, ratingThreshold, ratingThreshold, numGames));
		else if (myQuery == ANY_QUERY)
			filteredGames = MyDB.executeQuery(String.format(myQuery, numGames));
		
		setMode(Mode.TRAIN);
	}
	
	/** Close the ResultSet object that internally serves the game data. */
	public void close(){
		try {
			filteredGames.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int getNumGames(){
		return numGames;
	}
	
	public Mode getMode(){
		return myMode;
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
					filteredGames.getString("movelist"),
					filteredGames.getInt("id"));
			
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
