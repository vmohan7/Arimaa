package utilities;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import naive_bayes.GameData;
import naive_bayes.helper_classes.GameInfo;

import org.junit.Test;

import arimaa3.ArimaaMove;
import arimaa3.GameState;
import feature_extractor.FeatureExtractor;

public class UtilitiesTest {

	
	@Test
	public void testDB() {
		// Test whether the result set is not empty.
		ResultSet rs = MyDB.executeQuery("SELECT * FROM games limit 5"); 
		try {
			assertTrue(rs.next());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGameData(){
		GameData gd = new GameData(100, 0.7);
		HashSet<GameInfo> trainSet = new HashSet<GameInfo>();
		HashSet<GameInfo> testSet = new HashSet<GameInfo>();
		while (gd.hasNextGame()){
			GameInfo trainInfo = gd.getNextGame();
			System.out.println(trainInfo.getBlackStartState());
			trainSet.add(trainInfo);	
		}
		
		gd.setMode(GameData.Mode.TEST);
		while (gd.hasNextGame()){
			testSet.add(gd.getNextGame());
		}
		
		assertTrue(trainSet.size() == 70);
		assertTrue(testSet.size() == 30);
		trainSet.retainAll(testSet);
		assertTrue(trainSet.size() == 0);
	}


}
