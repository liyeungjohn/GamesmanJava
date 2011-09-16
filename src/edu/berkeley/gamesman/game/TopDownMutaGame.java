package edu.berkeley.gamesman.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.Record;
import edu.berkeley.gamesman.core.Value;
import edu.berkeley.gamesman.util.Pair;

/**
 * This is the super class for all games which contain their own state, but
 * should be solved via the top-down solver
 * 
 * @author dnspies
 */
public abstract class TopDownMutaGame extends Game<HashState> {

	/**
	 * The default constructor
	 * 
	 * @param conf
	 *            The configuration object
	 */
	public TopDownMutaGame(Configuration conf) {
		super(conf);
	}

	@Override
	public String displayState(HashState pos) {
		setToHash(pos.hash);
		return displayState();
	}

	/**
	 * "Pretty-print" the current State for display to the user
	 * 
	 * @return a pretty-printed string
	 */
	public abstract String displayState();

	@Override
	public void hashToState(long hash, HashState state) {
		state.hash = hash;
	}

	/**
	 * Sets the board position to the passed hash
	 * 
	 * @param hash
	 *            The hash to match
	 */
	public abstract void setToHash(long hash);

	@Override
	public Value primitiveValue(HashState pos) {
		setToHash(pos.hash);
		return primitiveValue();
	}

	@Override
	public Value strictPrimitiveValue(HashState pos) {
		setToHash(pos.hash);
		return strictPrimitiveValue();
	}

	/**
	 * @return The primitive value of the current position
	 */
	public abstract Value primitiveValue();

	@Override
	public long stateToHash(HashState pos) {
		return pos.hash;
	}

	/**
	 * @return The hash of the current position
	 */
	public abstract long getHash();

	@Override
	public String stateToString(HashState pos) {
		setToHash(pos.hash);
		return toString();
	}

	@Override
	public HashState stringToState(String pos) {
		setFromString(pos);
		return newState(getHash());
	}

	/**
	 * Sets the board to the position passed in string form
	 * 
	 * @param pos
	 *            The position to set to
	 */
	public abstract void setFromString(String pos);

	/**
	 * Makes a move on the board. The possible moves are ordered such that this
	 * will always be the move made when makeMove() is called
	 * 
	 * @return The number of available moves
	 */
	public abstract int makeMove();

	/**
	 * Changes the last move made to the next possible move in the list
	 * 
	 * @return If there are any more moves to be tried
	 */
	public abstract boolean changeMove();

	/**
	 * Undoes the last move made
	 */
	public abstract void undoMove();

	@Override
	public Collection<Pair<String, HashState>> validMoves(HashState pos) {
		setToHash(pos.hash);
		return validMoves();
	}

	private Collection<Pair<String, HashState>> validMoves() {
		List<String> moveStrings = moveNames();
		HashState[] states = new HashState[moveStrings.size()];
		for (int i = 0; i < states.length; i++) {
			states[i] = newState();
		}
		validMoves(states);
		ArrayList<Pair<String, HashState>> validMoves = new ArrayList<Pair<String, HashState>>(
				moveStrings.size());
		int i = 0;
		for (String move : moveStrings) {
			validMoves.add(new Pair<String, HashState>(move, states[i++]));
		}
		return validMoves;
	}

	/**
	 * @return A list of the names of the available moves, in the order they
	 *         would be returned by validMoves
	 */
	public abstract List<String> moveNames();

	@Override
	public Collection<HashState> startingPositions() {
		int numStartingPositions = numStartingPositions();
		ArrayList<HashState> startingPositions = new ArrayList<HashState>(
				numStartingPositions);
		for (int i = 0; i < numStartingPositions; i++) {
			setStartingPosition(i);
			HashState thisState = newState(getHash());
			startingPositions.add(thisState);
		}
		return startingPositions;
	}

	/**
	 * @return The number of starting positions for this game
	 */
	public abstract int numStartingPositions();

	/**
	 * Sets the internal state to the ith starting position
	 * 
	 * @param i
	 *            The starting position to set to
	 */
	public abstract void setStartingPosition(int i);

	@Override
	public int validMoves(HashState pos, HashState[] children) {
		setToHash(pos.hash);
		return validMoves(children);
	}

	private int validMoves(HashState[] children) {
		int numChildren = makeMove();
		for (int child = 0; child < numChildren; child++) {
			children[child].hash = getHash();
			changeMove();
		}
		undoMove();
		return numChildren;
	}

	@Override
	public HashState newState() {
		return new HashState();
	}

	private HashState newState(long hash) {
		return new HashState(hash);
	}

	@Override
	public void longToRecord(HashState recordState, long record, Record toStore) {
		setToHash(recordState.hash);
		longToRecord(record, toStore);
	}

	/**
	 * Unhashes a record using the current position as the game state
	 * 
	 * @param record
	 *            The hash of the record
	 * @param toStore
	 *            The record to store the result in
	 */
	public abstract void longToRecord(long record, Record toStore);

	@Override
	public long recordToLong(HashState recordState, Record fromRecord) {
		setToHash(recordState.hash);
		return recordToLong(fromRecord);
	}

	/**
	 * Hashes a record using the current position as the game state.
	 * 
	 * @param fromRecord
	 *            The record to hash
	 * @return The hashed value
	 */
	public abstract long recordToLong(Record fromRecord);

	/**
	 * Returns the actual primitive value according to the rules for the current
	 * state
	 * 
	 * @return The value of the current state
	 */
	public Value strictPrimitiveValue() {
		return primitiveValue();
	}
}
