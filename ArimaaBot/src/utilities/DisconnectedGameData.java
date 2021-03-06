package utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import utilities.helper_classes.GameInfo;

public class DisconnectedGameData implements AbstractGameData {
	private ResultSet filteredGames; 
	private ArrayList<Integer> ids;
	private int numGames;
	
	private Mode mode;
	
	private static final int RATING_THRESHOLD = 2100;
	// For reference, there are 279K total games, and 5K where both players are rated > 2100. 
	
	private static String discludeQuery = "SELECT games.id, w_state, b_state, movelist FROM " +
			"(SELECT id FROM games WHERE white_rating >= %d " +
			"AND black_rating >= %d AND id NOT IN (%s) " +
			"ORDER BY RAND() LIMIT %d ) g_ids " +
			"INNER JOIN games ON games.id = g_ids.id"; 
	
	private static String includeQuery = "SELECT games.id, w_state, b_state, movelist FROM " +
			"(SELECT id FROM games WHERE white_rating >= %d " +
			"AND black_rating >= %d AND id IN (%s) " +
			"ORDER BY RAND() LIMIT %d ) g_ids " +
			"INNER JOIN games ON games.id = g_ids.id"; 
	
	/**
	 * @param numGames The number of games to get from the database
	 * @param gameIds The ids (if any) to include or disclude
	 * @param toInclude A boolean indicating whether to include or disclude the given game Ids
	 */
	public DisconnectedGameData(int numGames, ArrayList<Integer> gameIds, boolean toInclude) {
		this.numGames = numGames + gameIds.size();
		String gIds = getConCatIds(gameIds);
		if (toInclude){
			filteredGames = MyDB.executeQuery(String.format(includeQuery, RATING_THRESHOLD, RATING_THRESHOLD, gIds, gameIds.size() ));
			mode = Mode.TRAIN;
		} else {
			filteredGames = MyDB.executeQuery(String.format(discludeQuery, RATING_THRESHOLD, RATING_THRESHOLD, gIds, numGames));
			mode = Mode.TEST;
		}
		
		try {
			if (hasNextGame())
				filteredGames.next();//in order to start at first game
		} 	catch (SQLException e) { e.printStackTrace(); } 
		ids = new ArrayList<Integer>();
	}
	
	public int getNumGames(){
		return numGames;
	}
	
	/** Close the ResultSet object that internally serves the game data. */
	public void close(){
		try {
			filteredGames.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String getConCatIds(ArrayList<Integer> gameIds) {
		if (gameIds == null || gameIds.size() == 0){
			return "''";
		}
		String ids = "" + gameIds.get(0);
		for (int i = 1; i < gameIds.size(); i++){
			ids += ", " + gameIds.get(i);
		}
		return ids;
	}

	public boolean hasNextGame(){
		try {
			return !filteredGames.isAfterLast();
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
			
			ids.add(filteredGames.getInt("id"));
			filteredGames.next();
			return ret;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public ArrayList<Integer> getCurrentGameId(){
		 return ids;
	}

	// TODO Implement "mode" for this class; this is so that results can be reported
	// in HypothesisTest.java as "train" or "test" set results. 
	public Mode getMode() {
		return mode;
	}
}
