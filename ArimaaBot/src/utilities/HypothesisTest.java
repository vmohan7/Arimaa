package utilities;

import utilities.helper_classes.GameInfo;

public class HypothesisTest {
	
	public static void test(AbstractHypothesis hyp, GameData gd) { //rename
		gd.setMode(GameData.Mode.TEST); //ensure that we are testing...
		
		while (gd.hasNextGame()) {
			GameInfo gi = gd.getNextGame();
			GameParser gp = new GameParser(gi);
			
			while (gp.hasNextGameState())
				evaluateMoveOrdering(gp.getNextGameState());
		}
		
		printStatistics(); //proper prototype?
	}
	/* for each game:
	 * 		gp = GameParser(game)
	 * 		while gp.hasMove:
	 * 			ArimaaState as = gp.next
	 * 			FeatureExtractor fe = FeatureExtractor(as.prev, as.curr)
	 * 			getAllMoves(as.curr)
	 * 			for each possible move:
	 * 				BitSet bs = fe.extract(possible move)
	 * 				weight = hypothesis.evaluate(bs)
	 * 			
	 * 			order the moves...
	 * 
	 * print statistics	
	 */
}
