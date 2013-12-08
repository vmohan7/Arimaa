package utilities;

import java.util.ArrayList;

import utilities.helper_classes.Utilities;
import ai_util.Util;
import arimaa3.ArimaaBaseClass;
import arimaa3.GameState;

public class GenCapturesWrapper extends arimaa3.GenCaptures 
								implements feature_extractor.FeatureConstants {
	
	/** The default size for an ArrayList containing the capture/trap integers */
	public static final int RECOMMENDED_N_CAPTURES = 20;

	public GenCapturesWrapper() {
		//Include this constructor? Or are we basically re-writing enough that we don't need this...
		super();
	}


	/** Returns an integer array, where each integer contains captures and traps for a move.
	 * NOTE: null is returned if no captures are possible. 
	 * @return Each int in the array contains [ms-byte: <*empty*> <*nSteps*> 
	 * 			<*trap*> <*pieceId*> :ls-byte]. There should be one bit per byte */
	public int[] getAllCapturesAndTraps(GameState game, boolean completeTurn) {
		//TODO: Test this
		initJeffBachersVariables(game, completeTurn);

		int allTraps = getTrapsToConsider(game);
		if (allTraps == 0) return null; //signify no traps

		
		ArrayList<Integer> allCapturesAndTrapsAL = new ArrayList<Integer>(RECOMMENDED_N_CAPTURES);
		byte[] davidWuPieceTypes = Utilities.calculatePieceTypes(game);
		
		for (int trap = 0; trap < TRAP.length; trap++) {
			//if trap isn't part of (i.e., the bit isn't set in) allTraps, continue
			if (   ( (1 << trap) & allTraps ) == 0   ) continue;
			
			//now trap is a valid trap to consider further
			
			//for all possible number of steps, add captures to the ArrayList
			for (int nSteps = 1; nSteps <= NUM_STEPS_IN_MOVE; nSteps++) {
				//use test_trap's return value to see what type of piece was captured...? 
				int pieceTypesCaptured = neemaTestTrap(game, trap, nSteps);
				
				while (pieceTypesCaptured != 0) {
					int type = Util.FirstOne(pieceTypesCaptured);
					pieceTypesCaptured ^= (1 << type);
					
					int pieceId = davidWuPieceTypes[type];
					int stepTrapCapture = 0;
					stepTrapCapture |= (1 << pieceId);
					stepTrapCapture |= (1 << (trap + Byte.SIZE));
					stepTrapCapture |= (1 << (nSteps + Short.SIZE)); //assume 2 bytes in a short
					allCapturesAndTrapsAL.add(stepTrapCapture);
				}
			}
			
		}
		
		if (allCapturesAndTrapsAL.isEmpty()) return null;

		return arrayListToIntArray(allCapturesAndTrapsAL);
	}


	/** Given nSteps, returns an integer with bits (0-11) on where pieceTypes were captured */
	private int neemaTestTrap(GameState game, int trap, int nSteps) {
		//TODO: Finish and test this...
		int allTypesCaptured = 0;
		
		/* JEFF's CODE ------------------------------------------------------- */
		{
		// Get some working variables
			long enemy_bb = game.colour_bb[game.enemy];
			long player_bb = game.colour_bb[game.player];
			int steps_available = nSteps; //game.getStepsRemaining();

			// Case 1: There is an epiece on the trap

			// I believe this case is complete!
			if ((enemy_bb & TRAP[trap]) != 0) {

				// By Definition an epiece must be touching trap
				long e_touch_bb = game.colour_bb[game.enemy] & TOUCH_TRAP[trap];
				int e_touch = Util.PopCnt(e_touch_bb);

				// Try and drag the epieces away with an already touching piece
				if (steps_available >= e_touch * 2) {
					if (determineCanPlayerUndefendTrap(game.player, trap, game)) {
						//find piece type on trap
						for (int pieceType = game.player; pieceType < 12; pieceType += 2) { //TODO: remove hardcoding
							if ((game.piece_bb[pieceType] & TRAP[trap]) != 0) {
								allTypesCaptured |= pieceType;
								break;
							}
						}
					}
						
					//try_drag_moves(game, e_touch_bb, FULL_BB, trap);
				}

				// Try and move a dominant piece next to the epiece touching the trap
//				int extra_steps = steps_available - 2 * e_touch;
//				if (extra_steps >= 1) {
//
//					// Given: there is only 1 epiece touching the trap
//					// Given: there is no epiece on the trap
//					// The precondition check catches 2 non dominant pieces touching trap
//					assert (Util.PopCnt(e_touch_bb) == 1);
//					assert ((enemy_bb & TRAP[trap]) != 0);
//
//					// Get the enemy piece type
//					int e_piece_type = game.getPieceType(e_touch_bb);
//
//					long p_dest_bb = ArimaaBaseClass.touching_bb(e_touch_bb); //changed from game.touching_bb
//					long p_stronger_bb = game.stronger_enemy_bb[e_piece_type];
//
//					// Try moving a dominant piece next to the epiece
//					try_two_steps(game, p_stronger_bb, p_dest_bb, trap, extra_steps);
//
//				}

			}

			// Case 2: There is no epiece on the trap AND
			// there are epiece(s) touching the trap

			// I believe this case is complete!
			// TODO algorithm misses player suiciding his piece on the trap.
			else if ((enemy_bb & TOUCH_TRAP[trap]) != 0) {

				// By Definition an epiece must be touching trap
				long e_touch_bb = game.colour_bb[game.enemy] & TOUCH_TRAP[trap];
				int e_touch = Util.PopCnt(e_touch_bb);

				// Try and drag an epiece onto the trap
				// Only works if there are at least 2 steps available
				if (steps_available >= 2) {
					long e_start_bb = TOUCH_TRAP[trap];
					long e_dest_bb = TRAP[trap];
					try_drag_moves(game, e_start_bb, e_dest_bb, trap);
				}

				// Try and move a dominant piece next to the epiece touching the trap
				int extra_steps = steps_available - 2 * e_touch;
				if (extra_steps >= 1) {

					// Given: there is only 1 epiece touching the trap
					// Given: there is no epiece on the trap
					// The precondition check catches 2 non dominant pieces touching trap
					assert (Util.PopCnt(e_touch_bb) == 1);
					assert ((enemy_bb & TRAP[trap]) == 0);

					// Get the enemy piece type
					int e_piece_type = game.getPieceType(e_touch_bb);

					long p_dest_bb = touching_bb(e_touch_bb);
					long p_stronger_bb = game.stronger_enemy_bb[e_piece_type];

					// Try moving a dominant piece next to the epiece
					try_two_steps(game, p_stronger_bb, p_dest_bb, trap, extra_steps);

				}

			}

			// Case 3: There is no epiece on the trap AND
			// there is no epiece touching the trap AND
			// there are epiece(s) 2 steps from the trap

			// THIS CASE IS COMPLETE!!!!!
			else if ((enemy_bb & TOUCH2_TRAP[trap]) != 0) {

				// Try and drag an epiece closer to the trap
				// Only works if there are at least 4 steps available
				if (steps_available >= 4) {
					long e_start_bb = TOUCH2_TRAP[trap];
					long e_dest_bb = TOUCH_TRAP[trap];
					try_drag_moves(game, e_start_bb, e_dest_bb, trap);
				}
			}

		}
		/* END JEFF's CODE -------------------------------------------------------*/
		
		
		return allTypesCaptured;
	}


	/** Returns an int containing bits 0-3 on (set to 1) if trap 0-3 is a possible trap */
	private int getTrapsToConsider(GameState game) {
		int allTraps = 0;
		for (int trap = 0; trap < TRAP.length; trap++)
			if (test_trap_precondition(game, trap)) allTraps |= (1 << trap);
		
		return allTraps;
	}
	
	
	/** Converts an ArrayList of Integers to an int[]. */
	private int[] arrayListToIntArray(ArrayList<Integer> list) {
		int[] ints = new int[list.size()];
		
		for (int i = 0; i < ints.length; i++)
			ints[i] = list.get(i);
		
		return ints;
	}


	/**
	 * Copied directly from GenCaptures!
	 * Performs some basics test to see if captures can be ruled out
	 * @param game GameState
	 * @param trap_id int
	 * @return boolean
	 */
	private boolean test_trap_precondition(GameState game, int trap_id) {

		trap_precondition_calls++;

		// If enemy elephant is touching trap, NO CAPTURES POSSIBLE!
		if ((TOUCH_TRAP[trap_id] & game.piece_bb[10 + game.enemy]) != 0) {
			trap_precondition_false++;
			return false;
		}

		// If 3 e pieces touch trap, NO CAPTURES POSSIBLE
		long e_touch_bb = game.colour_bb[game.enemy] & TOUCH_TRAP[trap_id];
		int e_touch = Util.PopCnt(e_touch_bb);
		if (e_touch >= 3) {
			trap_precondition_false++;
			return false;
		}

		// If 1 non dominated e piece touch trap
		// AND any other epiece touch trap, NO CAPTURE POSSIBLE
		long e_touch_non_dominated_bb = (e_touch_bb & ~game.dominated_pieces_bb);
		int e_touch_non_dominated = Util.PopCnt(e_touch_non_dominated_bb);
		if (e_touch_non_dominated >= 1 && e_touch >= 2) {
			trap_precondition_false++;
			return false;
		}

		if (game.getStepsRemaining() <= 3) {
			// If no epieces touch trap, NO CAPTURES POSSIBLE
			if (e_touch == 0) {
				trap_precondition_false++;
				return false;
			}

			// If two epieces touch trap, NO CAPTURES POSSIBLE
			if (e_touch >= 2) {
				trap_precondition_false++;
				return false;
			}

			// If one non=dominated epiece touch trap, NO CAPTURES POSSIBLE
			if (e_touch_non_dominated >= 1) {
				trap_precondition_false++;
				return false;
			}
		}

		return true;
	}

//	/** Copied directly from GenCaptures.java 
//	 * "Attempt to generate useful moves towards capturing an enemy piece" 
//	 * */
//	private void test_trap(GameState game, int trap_id) {
//
//		// Get some working variables
//		long enemy_bb = game.colour_bb[game.enemy];
//		long player_bb = game.colour_bb[game.player];
//		int steps_available = game.getStepsRemaining();
//
//		// Case 1: There is an epiece on the trap
//
//		// I believe this case is complete!
//		if ((enemy_bb & TRAP[trap_id]) != 0) {
//
//			// By Definition an epiece must be touching trap
//			long e_touch_bb = game.colour_bb[game.enemy] & TOUCH_TRAP[trap_id];
//			int e_touch = Util.PopCnt(e_touch_bb);
//
//			// Try and drag the epieces away with an already touching piece
//			if (steps_available >= e_touch * 2) {
//				try_drag_moves(game, e_touch_bb, FULL_BB, trap_id);
//			}
//
//			// Try and move a dominant piece next to the epiece touching the trap
//			int extra_steps = steps_available - 2 * e_touch;
//			if (extra_steps >= 1) {
//
//				// Given: there is only 1 epiece touching the trap
//				// Given: there is no epiece on the trap
//				// The precondition check catches 2 non dominant pieces touching trap
//				assert (Util.PopCnt(e_touch_bb) == 1);
//				assert ((enemy_bb & TRAP[trap_id]) != 0);
//
//				// Get the enemy piece type
//				int e_piece_type = game.getPieceType(e_touch_bb);
//
//				long p_dest_bb = ArimaaBaseClass.touching_bb(e_touch_bb); //changed from game.touching_bb
//				long p_stronger_bb = game.stronger_enemy_bb[e_piece_type];
//
//				// Try moving a dominant piece next to the epiece
//				try_two_steps(game, p_stronger_bb, p_dest_bb, trap_id, extra_steps);
//
//			}
//
//		}
//
//		// Case 2: There is no epiece on the trap AND
//		// there are epiece(s) touching the trap
//
//		// I believe this case is complete!
//		// TODO algorithm misses player suiciding his piece on the trap.
//		else if ((enemy_bb & TOUCH_TRAP[trap_id]) != 0) {
//
//			// By Definition an epiece must be touching trap
//			long e_touch_bb = game.colour_bb[game.enemy] & TOUCH_TRAP[trap_id];
//			int e_touch = Util.PopCnt(e_touch_bb);
//
//			// Try and drag an epiece onto the trap
//			// Only works if there are at least 2 steps available
//			if (steps_available >= 2) {
//				long e_start_bb = TOUCH_TRAP[trap_id];
//				long e_dest_bb = TRAP[trap_id];
//				try_drag_moves(game, e_start_bb, e_dest_bb, trap_id);
//			}
//
//			// Try and move a dominant piece next to the epiece touching the trap
//			int extra_steps = steps_available - 2 * e_touch;
//			if (extra_steps >= 1) {
//
//				// Given: there is only 1 epiece touching the trap
//				// Given: there is no epiece on the trap
//				// The precondition check catches 2 non dominant pieces touching trap
//				assert (Util.PopCnt(e_touch_bb) == 1);
//				assert ((enemy_bb & TRAP[trap_id]) == 0);
//
//				// Get the enemy piece type
//				int e_piece_type = game.getPieceType(e_touch_bb);
//
//				long p_dest_bb = touching_bb(e_touch_bb);
//				long p_stronger_bb = game.stronger_enemy_bb[e_piece_type];
//
//				// Try moving a dominant piece next to the epiece
//				try_two_steps(game, p_stronger_bb, p_dest_bb, trap_id, extra_steps);
//
//			}
//
//		}
//
//		// Case 3: There is no epiece on the trap AND
//		// there is no epiece touching the trap AND
//		// there are epiece(s) 2 steps from the trap
//
//		// THIS CASE IS COMPLETE!!!!!
//		else if ((enemy_bb & TOUCH2_TRAP[trap_id]) != 0) {
//
//			// Try and drag an epiece closer to the trap
//			// Only works if there are at least 4 steps available
//			if (steps_available >= 4) {
//				long e_start_bb = TOUCH2_TRAP[trap_id];
//				long e_dest_bb = TOUCH_TRAP[trap_id];
//				try_drag_moves(game, e_start_bb, e_dest_bb, trap_id);
//			}
//		}
//
//	}
	
	



	// INITIALIZATION (?)
	/** Initializes the variables in GenCaptures that Jeff Bacher initializes in genCaptures */
	private void initJeffBachersVariables(GameState initialPosition, boolean complete_turn) {
		this.complete_turn = complete_turn;
		// Need the frozen/dominated bb's
		initialPosition.compute_tertiary_bitboards();

		// Setup working variables
		//this.move_data = move_list; //-- don't need movelist?
		//this.gen_capture_calls++;
		this.saved_initial_position = initialPosition;
		//this.stack_depth = -1;
		this.repetition.increaseAge();
	}

}
