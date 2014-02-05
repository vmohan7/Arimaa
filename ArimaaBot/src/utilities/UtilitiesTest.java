package utilities;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.junit.Test;

import utilities.helper_classes.ArimaaState;
import utilities.helper_classes.GameInfo;
import utilities.helper_classes.Utilities;
import arimaa3.ArimaaMove;
import arimaa3.GameState;

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
	public void testGameData1(){
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
	
	@Test
	public void testGameData2(){
		GameData gd = new GameData(100, 0.7);
		HashSet<GameInfo> trainSet = new HashSet<GameInfo>();
		HashSet<GameInfo> testSet = new HashSet<GameInfo>();
		while (gd.hasNextGame()){
			GameInfo trainInfo = gd.getNextGame();
			trainSet.add(trainInfo);	
		}
		
		gd.setMode(GameData.Mode.TEST);
		while (gd.hasNextGame()){
			testSet.add(gd.getNextGame());
		}
		
		gd.setMode(GameData.Mode.TRAIN);
		while (gd.hasNextGame()){
			GameInfo trainInfo = gd.getNextGame();
			trainSet.remove(trainInfo);	
		}
		
		gd.setMode(GameData.Mode.TEST);
		while (gd.hasNextGame()){
			testSet.remove(gd.getNextGame());
		}
		
		assertTrue(trainSet.size() == 0);
		assertTrue(testSet.size() == 0);
	}

	@Test
	public void testGameParser() {
		String whiteSS = "1w Re2", blackSS = "1b rd7";
		String moveW1 = "2w Re2n Re3n Re4n", moveB1 = "\n2b rd7s rd6s rd5s";
		String moveW2 = "\n3w Re5e";
		String emptyMove = "\n3b";
		
		GameInfo gi = new GameInfo(whiteSS, blackSS, moveW1 + moveB1 + moveW2 + emptyMove, -1);
		GameParser gp = new GameParser(gi);
		
		assertTrue(gp.hasNextGameState());
		
		int loopCount = 0;
		GameState prevPrev = null;
		GameState prev = null;
		ArimaaMove prevPrevMove = null, prevMove = null;
		ArimaaState as = null; //init to silence compiler xD--this is initialized in the loop
		while (gp.hasNextGameState()) {
			as = gp.getNextGameState();
			assertTrue(as.getPrevPrev() == prevPrev);
			assertTrue(as.getPrev() == prev);
			assertTrue(as.getPrevMove() == prevMove);
			assertTrue(as.getPrevPrevMove() == prevPrevMove);
			
			System.out.println(as.getCurr().toBoardString());
			
			prev = as.getCurr();
			prevPrev = as.getPrev();
			prevMove = as.getNextMove();
			prevPrevMove = as.getPrevMove();
			loopCount++;
		}
		
		if (as != null)
			assertTrue(as.getNextMove().equals(new ArimaaMove(moveW2.substring(4)))); //test to ensure that this is the last move
		
		assertTrue(gp.getNextGameState() == null);
		assertTrue(loopCount == 3); //3 == numMoves
	}
	
	@Test
	public void testTime() {
		
		assertTrue(Utilities.msToString(1000).equals("1 second"));
		assertTrue(Utilities.msToString(2000).equals("2 seconds"));
		assertTrue(Utilities.msToString(1000000).equals("16 minutes, 40 seconds"));
		assertTrue(Utilities.msToString(1000000000).equals("11 days, 13 hours, 46 minutes, 40 seconds"));

		//SEE TODO to FIX THIS
		//System.out.println(Utilities.msToString(951_000_000));
		//System.out.println(Utilities.msToString(0));
		//assertTrue(Utilities.msToString(0).equals("0 seconds"));
		//assertTrue(Utilities.msToString(951_000_000).equals("11 days, 10 minutes"));
	}

	@Test
	public void testStepSources1() {
		assertArrayEquals(new byte[] {11, 10, 15, -1}, Utilities.getStepSources("Rd2n Rc2e Rc3x Ch2s"));
	}
	
	@Test
	public void testStepSources2() {
		assertArrayEquals(new byte[] {17, 9, 17, -1}, Utilities.getStepSources("rb3e rc3x Hb2n Hb3w"));
	}
	
	@Test
	public void testStepSources3() {
		assertArrayEquals(new byte[] {34, -1, -1, -1}, Utilities.getStepSources("dc5s"));
	}
}
