package feature_extractor;

import java.util.BitSet;
import arimaa3.GameState;

public class CaptureThreatsExtractor extends AbstractExtractor {

	private GameState prev, curr;
	
	/* Excerpt from Wu on Capture Threats
	 * ----------------------------------
	 * Threatening and defending the capture of a piece plays a key role in many tactics.
	 *		THREATENS CAP(type,s,trap): Threatens capture of opposing piece of type type (0-7)
	 *			in s (1-4) steps , in trap trap (0-3). (128 features)
	 *		INVITES CAP MOVED(type,s,trap): Moves own piece of type type (0-7) so that it can be
	 *		 	captured by opponent in s (1-4) steps , in trap trap (0-3). (128 features)
	 *		INVITES CAP UNMOVED(type,s,trap): Own piece of type type (0-7) can now be captured by
	 *			opponent in s (1-4) steps, in trap trap (0-3), but it was not itself moved. 
	 *			(128 features)
	 *		PREVENTS CAP(type,loc): Removes the threat of capture from own piece of type type (0-7)
	 *			at location loc (0-31). (256 features)
	 *			
	 *	The way in which one defends the capture of a piece is also important.
	 *		CAP DEF ELE(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap 
	 *			trap (0-3) by using the elephant as a trap defender. (16 features)
	 *		CAP DEF OTHER(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
	 *			trap (0-3) by using a non-elephant piece as a trap defender. (16 features)
	 *		CAP DEF RUNAWAY(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
	 *			trap (0-3) by making the threatened piece run away. (16 features)
	 *		CAP DEF INTERFERE(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
	 *			trap (0-3) by freezing or blocking the threatening piece. (16 features)
	 */
	
	/** Extracts Capture Threats information, using prev and curr 
	 * @param prev The previous game state (leading to curr)
	 * @param curr The current game state to be analyzed */
	public CaptureThreatsExtractor(GameState prev, GameState curr) {
		this.prev = prev;
		this.curr = curr;
	}
	
	
	/** The portion of the BitSet that will be updated is CAPTURE_THREATS_END - CAPTURE_THREATS_START
	 *  + 1 (== 704) bits. These bits will be split up as follows:
	 *  <br> -a
	 *  <br> -b 
	 *  <br> -c
	 *  <br> --anything to say?
	 *  */
	@Override
	public void updateBitSet(BitSet bitset) {
		int startOfRange = startOfRange();
		int numFeatures = endOfRange() - startOfRange + 1;
		
		//do stuff
	}

	@Override
	public int startOfRange() {
		return FeatureRange.CAPTURE_THREATS_START;
	}

	
	@Override
	public int endOfRange() {
		return FeatureRange.CAPTURE_THREATS_END;
	}
	
}