package feature_extractor;

import java.util.BitSet;

import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.GenCaptures;
import arimaa3.MoveList;

//TODO: Make this class as efficient as possible--I can totally see this being a bottleneck.
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
		//might need to have some more information passed around later on...
		threatensCap(bitset); 
	}

	@Override
	public int startOfRange() {
		return FeatureRange.CAPTURE_THREATS_START;
	}

	
	@Override
	public int endOfRange() {
		return FeatureRange.CAPTURE_THREATS_END;
	}
	
	/** Overloaded method-wrapper to give default bitset offset
	 * (See other method below for more comments). */
	private void threatensCap(BitSet bitset) {
		threatensCap(curr, bitset, CaptureThreats.THREATENS_CAP_OFFSET);
	}
	
	/** Updates the bitset with the THREATENS CAP features 
	 * @param curr is the GameState after the move is made (will be used to determine threats) 
	 * @param bitset is the bitset to be updated (as per recordMove)
	 * @param offset is the offset into the bit-vector where the bits will be updated <br>
	 *   <i>[Added mainly to support using this method later to update small bitsets (offset 0)
	 *   to pass information to other methods.]</i>*/
	private void threatensCap(GameState curr, BitSet bitset, int offset) {
		// getStepsRemaining has info about how many moves are left in a turn...!
		// move.steps has # steps in the move...
		
		
		int jeffBachersSize = 400000; //TODO: Figure out what the maximum size can be and use instead... (definitely way less than 400,000)
		boolean completeTurn = false; //allows us to get captures with fewer than 4 moves
		MoveList moveList = new MoveList(jeffBachersSize);
		
		GenCaptures captures = new GenCaptures();
		
		/* Play a pass-move for the opponent in order to see threats made by original move 
		 * (we need to check possible captures for the player who just played) */
		GameState currOppPass = new GameState();
		currOppPass.playPASS(curr); //copies curr into currPassOpp before playing pass
		captures.genCaptures(currOppPass, moveList, completeTurn); //fills moveList with capture threats
		
		for (ArimaaMove move : moveList) {//seems to avoid null moves...
			GameState postCapture = new GameState();
			postCapture.playFullClear(move, currOppPass);
			recordCaptureMove(curr, postCapture, bitset, offset);
		}
		
		
//		System.out.println("Capture Moves: ");
//		System.out.println("Initial board: \n" + currPassOpp.toBoardString() + "\n");
//		for (ArimaaMove move : moveList) {
//			if (move == null) continue;
//			
//			GameState currCopy = new GameState();
//			currCopy.playFullClear(move, currPassOpp);
//			
//			System.out.println(move.toString());
//			System.out.println(currCopy.toBoardString());
//		}
//		System.out.println();
		
		
	}
	
	 /** The bitset is updated as follows: <br>
	 *  ->For each trap, for each step, the piece type. (32 * trap + 8 * step + type) <br> 
	 *  -><i>[32 is the number of bits taken by all (step,type) combinations. 8 is the number</i>
	 *  of bits taken by all types.] 
	 *  @param preCapture the GameState before any capture (the "threatening, original" game state)
	 *  @param postCapture the GameState after a capturing move has been played
	 *  @param toUpdate the BitSet that will be updated (as above)
	 *  @param bitOffset the offset into the BitSet relative to which point bits will be set
	 *  */
	private void recordCaptureMove(GameState preCapture, GameState postCapture,
								BitSet toUpdate, int bitOffset) {
		
	}
	
}
