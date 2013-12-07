package feature_extractor;

import arimaa3.Constants;

/** Extends Constants to add more constants (for the Feature Extractor in particular) */
public interface FeatureConstants extends Constants {
	
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
		
	/** Contains the constants used in SteppingOnTrapsExtractor
	 * @author Neema */
	public static class SteppingOnTraps {
		
		/** These are the piece types for the bit-boards long[6] stored in SteppingOnTrapsExtractor */
		/* These don't seem to be used just yet... update this comment if this changes... Really
		 * just here for the sake of clarity */
		public static final byte RABBIT =	PT_WHITE_RABBIT / 2,
								 CAT = 		PT_WHITE_CAT / 2,
								 DOG = 		PT_WHITE_DOG / 2,
								 HORSE = 	PT_WHITE_HORSE / 2,
								 CAMEL = 	PT_WHITE_CAMEL / 2,
								 ELEPHANT = PT_WHITE_ELEPHANT / 2;
	}
	
	
	/** FeatureRange contains the constants describing the <i>start</i> and <i>end</i>
	 *  indices inclusive (in the FeatureExtractor's BitSet) of the different AbstractExtractor subclasses. */
	public static class FeatureRange {
		
		/** There are 1040 Position Movement features set by the PositionMovementExtractor class. */
		public static final int POS_MVMT_START = 0, POS_MVMT_END = 1039;
		
		/** There are 512 Trap Status features set by the TrapExtractor class. */
		public static final int TRAP_STATUS_START = 1040, TRAP_STATUS_END = 1551;

		/** There are 160 Freezing features set by the FreezingExtractor class. */
		public static final int FREEZING_START = 1552, FREEZING_END = 1711;
		
		/** There are 64 features set by the SteppingOnTrapsExtractor class. */
		public static final int STEPPING_TRAP_START = 1712, STEPPING_TRAP_END = 1775;
		
		/** There are 704 Capture Threats features set by the CaptureThreatsExtractor class. */
		public static final int CAPTURE_THREATS_START = 1776, CAPTURE_THREATS_END = 2479;
		
		/** There are 128 Previous Moves features set by the PreviousMovesExtractor class. */
		public static final int PREV_MOVES_START = 2480, PREV_MOVES_END = 2607;
		
		/** The index of the last feature in the vector */
		/* TODO: Update to the last feature as features are added*/
		public static final int MAX_END = PREV_MOVES_END;
	}
	
	/** The total number of features in our feature vector. */
	public static final int NUM_FEATURES = FeatureRange.MAX_END + 1;
	
	/** There are 32 locations on the board taking left-right symmetry into account. */
	public static final int NUM_LOCATIONS = 32;
	
	/** There are 8 piece types as described in David Wu's paper pg 25. */
	public static final int NUM_PIECE_TYPES = 8;
	
	/** 
	 * Number of closeness score values for current move compared with previous move
	 * called LAST_CLOSENESS. 
	 **/
	public static final int NUM_CLOSENESS_SCORES = 64;
}
