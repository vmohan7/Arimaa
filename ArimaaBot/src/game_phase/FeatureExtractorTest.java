package game_phase;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;

import org.junit.Test;

import utilities.GoalTestWrapper;
import arimaa3.ArimaaMove;
import arimaa3.Constants;
import arimaa3.GameState;

public class FeatureExtractorTest implements Constants {

	private static String tests[] = {
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|         D   r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   R             |%135|                 |%134|                 |%133|             r   |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"2w %13 +-----------------+%138| r r   H r r r r |%137|   e   C r   r   |%136|   d X     X     |%135|   d R M c       |%134|       R         |%133|     X     X     |%132|                 |%131|       R       R |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   e             |%135| R               |%134|                 |%133| R               |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136|   E             |%135| r               |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135|                 |%134| r               |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135| c               |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| m E             |%135|                 |%134|                 |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
		"12w %13 +-----------------+%138|                 |%137|                 |%136| r E             |%135|                 |%134| c               |%133|                 |%132|                 |%131|                 |%13 +-----------------+%13   a b c d e f g h%13",
	};
	
	@Test
	public void testMovementFeatures1() {
		
		// data from first game in "games" relation
	    String white = "1w Ee2 Md2 Ha2 Hh2 Db2 Dg2 Cf2 Cc1 Ra1 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String black = "1b ee7 md7 ch8 ca8 dc7 hb7 hg7 df7 ra7 rh7 rb8 rc8 rd8 re8 rf8 rg8";
	    GameState startState = new GameState(white,black);

	    double[] features = FeatureExtractor.extractFeatures(startState);
	    
	    System.out.println(startState.toBoardString());
	    
	    for(int i = 0; i < features.length; i++){
	    	System.out.println(i + " : " + features[i]);
	    }

	}
	
	@Test
	public void testTrapDetect() {
		
		// data from first game in "games" relation
	    String white = "1w Ec3 Mc2"; 
	    String black = "1b eg6";
	    GameState startState = new GameState(white,black);

	    double[] features = FeatureExtractor.extractFeatures(startState);
	    
	    System.out.println(startState.toBoardString());
	    
	    for(int i = 98; i < features.length; i++){
	    	System.out.println(i + " : " + features[i]);
	    }

	}
	
	@Test
	public void testReducedFeatureSet() {
		System.out.println("\n\n==== BEGIN TESTING REDUCED FEATURE SET ====");
	    String gold = "1w Ee3 Md2 Ha4 Hh2 Db4 Dg2 Cf2 Cc1 Re6 Rb1 Rd1 Re1 Rf1 Rg1 Rh1 Rc2"; 
	    String silver = "1b ec5 md7 ch8 ca8 dc7 hb7 hg7 df5 ra7 rh7 ra2 rc8 rd8 rf8 rg8";
	    GameState state = new GameState(gold,silver);
	    System.out.println(state.toBoardString());
	    
	    String player = state.player == 0 ? "gold" : "silver";
		System.out.println(player + "'s turn");

		
	    double[] reducedFeatures = FeatureExtractor.extractReducedFeatures(state);
	   
	    for(int i = 0; i < reducedFeatures.length; i++){
	    	System.out.println(i + " : " + reducedFeatures[i]);
	    }
	    
		System.out.println("\n\n==== END TESTING REDUCED FEATURE SET ====");


	}

	
}
