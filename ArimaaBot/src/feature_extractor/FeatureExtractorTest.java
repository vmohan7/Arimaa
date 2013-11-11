package feature_extractor;

import static org.junit.Assert.*;

import org.junit.Test;

import arimaa3.ArimaaMove;
import arimaa3.GameState;

public class FeatureExtractorTest {

	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
	};
	
	@Test
	public void testMovementFeatures1() {
		
		// data from first game in "games" relation
	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
	    GameState startState = new GameState(white,black);
	    FeatureExtractor fe = new FeatureExtractor(startState, null);
	    fe.extractFeatures(new ArimaaMove("Db2n Ha2n Ha3n Hh2n"));
	    
	    System.out.println(fe.featureVector.toString());
	    
	    assertTrue(fe.featureVector.get(620));
	    assertTrue(fe.featureVector.get(68));
	    assertTrue(fe.featureVector.get(101));
	    assertTrue(fe.featureVector.get(586));
	    assertTrue(fe.featureVector.get(590));
	    
//	    System.out.println(startState.toBoardString());
//	    System.out.println(fe.curr.toBoardString());
	}
	
	@Test
	public void testMovementFeatures2() {
	    GameState startState = new GameState(tests[2]);
	    FeatureExtractor fe = new FeatureExtractor(startState, null);
	    fe.extractFeatures(new ArimaaMove("db6e dc6x ce5s ce4s ce3s"));

	    assertTrue(fe.featureVector.get(339));
	    assertTrue(fe.featureVector.get(341));
	    assertTrue(fe.featureVector.get(849));
	    assertTrue(fe.featureVector.get(874));
	    
//	    System.out.println(fe.featureVector.toString());
//	    System.out.println(startState.toBoardString());
//	    System.out.println(fe.curr.toBoardString());
	}
	
	@Test
	public void testLocationMappings(){

		assertTrue(FeatureExtractor.getLocation(0) == 0); 
		assertTrue(FeatureExtractor.getLocation(5) == 2);
		assertTrue(FeatureExtractor.getLocation(10) == 6);
		assertTrue(FeatureExtractor.getLocation(15) == 4);
		assertTrue(FeatureExtractor.getLocation(57) == 29);
	}

}
