package feature_extractor;

import java.util.BitSet;

import utilities.helper_classes.Utilities;

import arimaa3.ArimaaMove;
import arimaa3.GameState;
import arimaa3.GenTurn;

public class PreviousMovesExtractor extends AbstractExtractor {
	
	byte[] prevPrevStepSources;
	byte[] prevStepSources;
	byte[] currStepSources;
	private BitSet featureVector;
	
	public PreviousMovesExtractor(GameState prevPrevSource, ArimaaMove prevPrevMove, GameState prevSource, ArimaaMove prevMove, GameState currSource,  ArimaaMove currMove) {
		
		GenTurn turnGenerator = new GenTurn();
		if(prevPrevSource!=null && prevPrevMove!=null) {
			String prevPrevMoveStr = turnGenerator.getOfficialArimaaNotation(prevPrevSource, prevPrevMove);
			prevPrevStepSources = Utilities.getStepSources(prevPrevMoveStr);
		}
		if(prevSource!=null && prevMove!=null) {
			String prevMoveStr = turnGenerator.getOfficialArimaaNotation(prevSource, prevMove);
			prevStepSources = Utilities.getStepSources(prevMoveStr);
		}
		if(currSource!=null && currMove!=null) {
			String currMoveStr = turnGenerator.getOfficialArimaaNotation(currSource, currMove);
			currStepSources = Utilities.getStepSources(currMoveStr);
		}
		featureVector = null;
	}

	@Override
	public void updateBitSet(BitSet featureVector) {
		this.featureVector = featureVector;
		int lastCloseness = getClosenessScore(currStepSources, prevStepSources);
		int lastLastCloseness = getClosenessScore(currStepSources, prevPrevStepSources);
		setClosenessScores(lastCloseness, lastLastCloseness);
	}

	private int getClosenessScore(byte[] stepSources1, byte[] stepSources2) {
		int score = 0;
		if(stepSources1 != null && stepSources2 != null) {
			for(int i = 0; i < stepSources1.length && stepSources1[i] != -1; i++) {
				for(int j = 0; j < stepSources2.length && stepSources2[j] != -1; j++) {
					score += Math.max(0, 4 - getManhattanDistance(stepSources1[i], stepSources2[j]));
				}
			}
		}
		return score;
	}
	
	private void setClosenessScores(int lastCloseness, int lastLastCloseness) {

		featureVector.set(startOfRange() + lastCloseness);
		featureVector.set(startOfRange() + NUM_CLOSENESS_SCORES + lastLastCloseness);
	}
	
	private int getManhattanDistance(byte square1, byte square2) {
		int verticalDistance = Math.abs((square1/8) - (square2/8));
		int horizontalDistance = Math.abs((square1 % 8) - (square2 % 8));
		return verticalDistance + horizontalDistance;
	}
	
	@Override
	public int startOfRange() {
		return FeatureRange.PREV_MOVES_START;
	}

	@Override
	public int endOfRange() {
		return FeatureRange.PREV_MOVES_END;
	}

}
