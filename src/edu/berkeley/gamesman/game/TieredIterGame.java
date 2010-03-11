package edu.berkeley.gamesman.game;

import java.util.ArrayList;
import java.util.Collection;

import edu.berkeley.gamesman.core.PrimitiveValue;
import edu.berkeley.gamesman.game.util.ItergameState;
import edu.berkeley.gamesman.util.Pair;

/**
 * @author DNSpies
 */
public abstract class TieredIterGame extends TieredGame<ItergameState> {

	@Override
	public PrimitiveValue primitiveValue(ItergameState pos) {
		setState(pos);
		return primitiveValue();
	}

	/**
	 * Assumes the given state.
	 * 
	 * @param pos
	 *            The position to assume
	 */
	public abstract void setState(ItergameState pos);

	/**
	 * @return The "primitive value" of the current position.
	 */
	public abstract PrimitiveValue primitiveValue();

	@Override
	public Collection<Pair<String, ItergameState>> validMoves(ItergameState pos) {
		setState(pos);
		return validMoves();
	}

	@Override
	public int validMoves(ItergameState pos, ItergameState[] children) {
		setState(pos);
		return validMoves(children);
	}

	/**
	 * @return The states of all the possible moves from this position.
	 */
	public abstract Collection<Pair<String, ItergameState>> validMoves();

	/**
	 * @return Whether there is another position.
	 */
	public boolean hasNext() {
		return hasNextHashInTier() || getTier() < numberOfTiers();
	}

	/**
	 * @return The tier of this position
	 */
	public abstract int getTier();

	/**
	 * Cycle to the next position.
	 */
	public void next() {
		if (hasNextHashInTier())
			nextHashInTier();
		else
			setTier(getTier() + 1);
	}

	@Override
	public String stateToString(ItergameState pos) {
		setState(pos);
		return stateToString();
	}

	/**
	 * @return A string representation of this position.
	 */
	public abstract String stateToString();

	@Override
	public ItergameState stringToState(String pos) {
		setFromString(pos);
		return getState();
	}

	/**
	 * @param pos
	 *            A string representing this position
	 */
	public abstract void setFromString(String pos);

	/**
	 * Returns a state object for this position
	 * 
	 * @return The state of this position
	 */
	public abstract ItergameState getState();

	@Override
	public String displayState(ItergameState pos) {
		setState(pos);
		return displayState();
	}

	/**
	 * @param tier
	 *            The tier in question
	 * @return The number of hashes in the tier
	 */
	public long numHashesForTier(int tier) {
		setTier(tier);
		return numHashesForTier();
	}

	/**
	 * Sets this game to the given tier.
	 * 
	 * @param tier
	 *            The tier to set to.
	 */
	public abstract void setTier(int tier);

	/**
	 * Pretty-print's the current position
	 * 
	 * @return a string of the position
	 */
	public abstract String displayState();

	/**
	 * @return The number of hashes in this tier.
	 */
	public abstract long numHashesForTier();

	@Override
	public Collection<ItergameState> startingPositions() {
		ArrayList<ItergameState> positions = new ArrayList<ItergameState>();
		for (int i = 0; i < numStartingPositions(); i++) {
			setStartingPosition(i);
			positions.add(getState());
		}
		return positions;
	}

	/**
	 * Sets this board to a starting position.
	 * 
	 * @param n
	 *            The number position to set to
	 */
	public abstract void setStartingPosition(int n);

	/**
	 * @return The number of possible starting positions.
	 */
	public abstract int numStartingPositions();

	/**
	 * @return Is there another hash for this tier?
	 */
	public abstract boolean hasNextHashInTier();

	/**
	 * Cycles to the next hash in this tier
	 */
	public abstract void nextHashInTier();

	@Override
	public long stateToHash(ItergameState pos) {
		return myHasher.hashOffsetForTier(pos.tier) + pos.hash;
	}

	public abstract int numberOfTiers();

	@Override
	public abstract int maxChildren();

	/**
	 * Stores all the valid moves for this position in moves
	 * 
	 * @param moves
	 *            An array to store to
	 * @return The number of moves stored
	 */
	public abstract int validMoves(ItergameState[] moves);

	@Override
	public ItergameState newState() {
		return new ItergameState();
	}
}