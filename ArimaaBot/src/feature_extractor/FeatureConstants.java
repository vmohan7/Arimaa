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
	}
	
	public static class FeatureRange {
		
		/** There are 512 Trap Status features set by the TrapExtractor class. */
		public static final int TRAP_STATUS_START = 1040, TRAP_STATUS_END = 1551;
	}
	
	
	
}
