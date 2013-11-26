package feature_extractor;

public interface FeatureConstants {
	
	/**
	 * Status number -> trap "condition"
	 * <br> 0		 -> 0 pieces around a trap
	 * <br> 1		 -> 1 piece touching trap (`No `Elephant)
	 * <br>	2		 -> 1 piece touching trap (`Elephant)
	 * <br>	3		 -> 2 pieces touching trap (`No `Elephant)
	 * <br>	4		 -> 2 pieces touching trap (`Elephant)
	 * <br>	5		 -> 3 pieces touching trap (`No `Elephant)
	 * <br>	6		 -> 3 pieces touching trap (`Elephant)
	 * <br>	7		 -> 4 pieces touching trap (indifferent to presence of elephant)
	 * @author Neema
	 */
	public static class TrapStatus {
		/** 0 pieces around a trap */
		public static final byte ZERO	  = 0;
		/** 1 piece touching trap (`No `Elephant) */
		public static final byte ONE_NE   = 1;
		/** 1 piece touching trap (`Elephant) */
		public static final byte ONE_E    = 2;
		/** 2 pieces touching trap (`No `Elephant) */
		public static final byte TWO_NE   = 3;
		/** 2 pieces touching trap (`Elephant) */
		public static final byte TWO_E    = 4;
		/** 3 pieces touching trap (`No `Elephant) */
		public static final byte THREE_NE = 5;
		/** 3 pieces touching trap (`Elephant) */
		public static final byte THREE_E  = 6;
		/** 4 pieces touching trap (indifferent to presence of elephant) */
		public static final byte FOUR     = 7;
		/** The number of possible TrapStatus-es for a given player and trap */
		public static final byte NUM_STATUSES = 8;
		
		/** Converts the number of pieces touching (the trap) + presence of an elephant
		 * to one of the NUM_STATUSES statuses. (See TrapStatus class' comment for an
		 * explanation.)*/
		public static byte convertToStatus(byte numAdjacent, boolean elephant) {
			if (numAdjacent == 0) return ZERO;
			if (numAdjacent == 4) return FOUR;
			
			return (byte)(2 * numAdjacent - (elephant ? 0 : 1));
		}
	}
	
	/** There are eight threats in total. (See David Wu's paper p. 26, or 
	 * CaptureThreatsExtractor.java) <br>
	 * The first four are threatening and defending captures.
	 * The last four are the method by which the capture was defended.
	 * @author Neema */
	public static class CaptureThreats {	
		
		/** THREATENS CAP(type,s,trap): Threatens capture of opposing piece of type type (0-7)
		 *			in s (1-4) steps , in trap trap (0-3). (128 features) */
		public static final int THREATENS_CAP_OFFSET = FeatureRange.CAPTURE_THREATS_START;
		
		/**	INVITES CAP MOVED(type,s,trap): Moves own piece of type type (0-7) so that it can be
		 *		 	captured by opponent in s (1-4) steps , in trap trap (0-3). (128 features) */
		public static final int INVITES_CAP_MOVED_OFFSET = THREATENS_CAP_OFFSET + 128;
		
		/**	INVITES CAP UNMOVED(type,s,trap): Own piece of type type (0-7) can now be captured by
		 *			opponent in s (1-4) steps, in trap trap (0-3), but it was not itself moved. 
		 * 			(128 features)*/ 
		public static final int INVITES_CAP_UNMOVED_OFFSET = INVITES_CAP_MOVED_OFFSET + 128;
		
		/**	PREVENTS CAP(type,loc): Removes the threat of capture from own piece of type type (0-7)
		 *			at location loc (0-31). (256 features) */
		public static final int PREVENTS_CAP_OFFSET = INVITES_CAP_UNMOVED_OFFSET + 128;
		
		/**	CAP DEF ELE(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap 
		 *			trap (0-3) by using the elephant as a trap defender. (16 features) */
		public static final int CAP_DEF_ELE_OFFSET = PREVENTS_CAP_OFFSET + 256;
		
		/**	CAP DEF OTHER(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
		 *			trap (0-3) by using a non-elephant piece as a trap defender. (16 features) */
		public static final int CAP_DEF_OTHER_OFFSET = CAP_DEF_ELE_OFFSET + 16;
		
		/**	CAP DEF RUNAWAY(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
		 *			trap (0-3) by making the threatened piece run away. (16 features) */
		public static final int CAP_DEF_RUNAWAY = CAP_DEF_OTHER_OFFSET + 16;
		 
		/**	CAP DEF INTERFERE(trap,s): Defends own piece otherwise capturable in s (1-4) steps in trap
		 *			trap (0-3) by freezing or blocking the threatening piece. (16 features) */
		public static final int CAP_DEF_INTERFERE = CAP_DEF_RUNAWAY + 16;
		//assert(CAP_DEF_INTERFERE + 16 == FeatureRange.CAPTURE_THREATS_END + 1);
		
		/** Contains the offsets for each sub-feature class, in order of description by David Wu.
		 * (OFFSET_ARRAY[0] is the first sub-feature class described by Wu, p. 26) */
		public static final int[] OFFSET_ARRAY = {
			THREATENS_CAP_OFFSET, INVITES_CAP_MOVED_OFFSET, INVITES_CAP_UNMOVED_OFFSET,
				PREVENTS_CAP_OFFSET, 
			CAP_DEF_ELE_OFFSET, CAP_DEF_OTHER_OFFSET, CAP_DEF_RUNAWAY, CAP_DEF_INTERFERE
				
		};
		
	}
	
	/** FeatureRange contains the constants describing the <i>start</i> and <i>end</i>
	 *  indices (in the FeatureExtractor's BitSet) of the different AbstractExtractor subclasses. */
	public static class FeatureRange {
		
		/** There are 1040 Position Movement features set by the PositionMovementExtractor class. */
		public static final int POS_MVMT_START = 0, POS_MVMT_END = 1039;
		
		/** There are 512 Trap Status features set by the TrapExtractor class. */
		public static final int TRAP_STATUS_START = 1040, TRAP_STATUS_END = 1551;
		
		/** There are 704 Capture Threats features set by the CaptureThreatsExtractor class. */
		public static final int CAPTURE_THREATS_START = 1776, CAPTURE_THREATS_END = 2479;
		
		/** The index of the last feature in the vector */
		/* Update to the last feature as features are added*/
		public static final int MAX_END = CAPTURE_THREATS_END;
	}
	
	
	/** The total number of features in our feature vector. */
	public static final int NUM_FEATURES = FeatureRange.MAX_END + 1;
	
}
