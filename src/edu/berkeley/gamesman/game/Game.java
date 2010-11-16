package edu.berkeley.gamesman.game;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.Value;
import edu.berkeley.gamesman.core.Record;
import edu.berkeley.gamesman.core.State;
import edu.berkeley.gamesman.util.Pair;

/**
 * Public interface that all Games must implement to be solvable
 * 
 * @author David Spies
 * @param <S>
 *            The object used to represent a Game State
 * 
 */
public abstract class Game<S extends State> {

	protected final Configuration conf;

	/**
	 * @param conf
	 *            The configuration object
	 */
	public Game(Configuration conf) {
		this.conf = conf;
	}

	/**
	 * Generates all the valid starting positions
	 * 
	 * @return a Collection of all valid starting positions
	 */
	public abstract Collection<S> startingPositions();

	/**
	 * Given a board state, generates all valid board states one move away from
	 * the given state. The String indicates in some sense what move is made to
	 * reach that position. Also override the other validMoves (to be used by
	 * the solver)
	 * 
	 * @param pos
	 *            The board state to start from
	 * @return A <move,state> pair for all valid board states one move forward
	 * @see Game#validMoves(State,State[])
	 */
	public abstract Collection<Pair<String, S>> validMoves(S pos);

	/**
	 * A synchronized implementation of validMoves for JSONInterface (When
	 * solving, the game is cloned to ensure there are no synchronization
	 * problems)
	 * 
	 * @param pos
	 *            The current position
	 * @return A collection of each move along with its identifying string
	 */
	public synchronized final Collection<Pair<String, S>> synchronizedValidMoves(
			S pos) {
		return validMoves(pos);
	}

	/**
	 * Valid moves without instantiation. Pass in a State array which the
	 * children will be stored in.
	 * 
	 * @param pos
	 *            The board state to start from
	 * @param children
	 *            The array to store all valid board states one move forward
	 * @return The number of children for this position
	 */
	public abstract int validMoves(S pos, S[] children);

	/**
	 * @return The maximum number of child states for any position
	 */
	public abstract int maxChildren();

	/**
	 * Applies move to pos
	 * 
	 * @param pos
	 *            The State on which to apply move
	 * @param move
	 *            A String for the move to apply to pos
	 * @return The resulting State, or null if it isn't found in validMoves()
	 */
	public S doMove(S pos, String move) {
		for (Pair<String, S> next : validMoves(pos))
			if (next.car.equals(move))
				return next.cdr;
		return null;
	}

	/**
	 * Given a primitive board state, return how good it is. Return 0 if any
	 * winning/losing position is equal. Otherwise, return the value of this
	 * endgame such that the best possible ending state is 0 and worse states
	 * are higher.
	 * 
	 * @param pos
	 *            The primitive State
	 * @return the score of this position
	 */
	public int primitiveScore(S pos) {
		return 0;
	}

	/**
	 * A synchronized implementation of primitiveScore for JSONInterface (When
	 * solving, the game is cloned to ensure there are no synchronization
	 * problems)
	 * 
	 * @param pos
	 *            The position
	 * @return The position's score
	 */
	public synchronized final int synchronizedPrimitiveScore(S pos) {
		return primitiveScore(pos);
	}

	/**
	 * @return The number of players, 2 for a game, 1 for a puzzle
	 */
	public int getPlayerCount() {
		return 2;
	}

	/**
	 * Given a board state return its primitive "value". Usually this value
	 * includes WIN, LOSE, and perhaps TIE Return UNDECIDED if this is not a
	 * primitive state
	 * 
	 * @param pos
	 *            The primitive State
	 * @return the primitive value of the state
	 */
	public abstract Value primitiveValue(S pos);

	/**
	 * @param pos
	 *            The current position
	 * @return The primitive value of the position. Generally LOSE,TIE, or
	 *         UNDECIDED (for positions which aren't primitive)
	 */
	public synchronized final Value synchronizedPrimitiveValue(S pos) {
		return primitiveValue(pos);
	}

	/**
	 * Unhash a given hashed value and return the corresponding Board
	 * 
	 * @param hash
	 *            The hash given
	 * @return the State represented
	 */
	public final S hashToState(long hash) {
		S res = newState();
		hashToState(hash, res);
		return res;
	}

	/**
	 * Hash a given state into a hashed value
	 * 
	 * @param pos
	 *            The State given
	 * @return The hash that represents that State
	 */
	public abstract long stateToHash(S pos);

	/**
	 * A synchronized implementation of stateToHash for JSONInterface (When
	 * solving, the game is cloned to ensure there are no synchronization
	 * problems)
	 * 
	 * @param pos
	 *            The position
	 * @return The position's hash
	 */
	public synchronized final long synchronizedStateToHash(S pos) {
		return stateToHash(pos);
	}

	/**
	 * Produce a machine-parsable String representing the state. This function
	 * must be the exact opposite of stringToState
	 * 
	 * @param pos
	 *            the State given
	 * @return a String
	 * @see Game#stringToState(String)
	 */
	public abstract String stateToString(S pos);

	/**
	 * A synchronized implementation of stateToString for JSONInterface (When
	 * solving, the game is cloned to ensure there are no synchronization
	 * problems)
	 * 
	 * @param pos
	 *            The position
	 * @return A string representing the position
	 */
	public synchronized final String synchronizedStateToString(S pos) {
		return stateToString(pos);
	}

	/**
	 * "Pretty-print" a State for display to the user
	 * 
	 * @param pos
	 *            The state to display
	 * @return a pretty-printed string
	 */
	public abstract String displayState(S pos);

	/**
	 * "Pretty-print" a State for display by Graphviz/Dotty. See
	 * http://www.graphviz.org/Documentation.php for documentation. By default,
	 * replaces newlines with <br />
	 * . Do not use a
	 * <table>
	 * here!
	 * 
	 * @param pos
	 *            The GameState to format.
	 * @return The html-like formatting of the string.
	 */
	public String displayHTML(S pos) {
		return displayState(pos).replaceAll("\n", "<br align=\"left\"/>");
	}

	/**
	 * Given a String construct a State. This <i>must</i> be compatible with
	 * stateToString as it is used to send states over the network.
	 * 
	 * @param pos
	 *            The String given
	 * @return a State
	 * @see Game#stateToString(State)
	 */
	public abstract S stringToState(String pos);

	/**
	 * A synchronized implementation of stringToState for JSONInterface (When
	 * solving, the game is cloned to ensure there are no synchronization
	 * problems)
	 * 
	 * @param pos
	 *            The position as a string
	 * @return The resulting state
	 */
	public synchronized final S synchronizedStringToState(String pos) {
		return stringToState(pos);
	}

	/**
	 * @return a String that uniquely describes the setup of this Game
	 *         (including any variant information, game size, etc)
	 */
	public abstract String describe();

	/**
	 * @return The total number of hashes
	 */
	public abstract long numHashes();

	/**
	 * @return The total number of possible states a record could be
	 */
	public abstract long recordStates();

	/**
	 * For mutable states. Avoids needing to instantiate new states.
	 * 
	 * @param hash
	 *            The hash to use
	 * @param s
	 *            The state to store the result in
	 */
	public abstract void hashToState(long hash, S s);

	/**
	 * @return A new empty state
	 */
	public abstract S newState();

	/**
	 * @param len
	 *            The number of states
	 * @return A new array with len states
	 */
	@SuppressWarnings("unchecked")
	public final S[] newStateArray(int len) {
		S oneState = newState();
		S[] arr = (S[]) Array.newInstance(oneState.getClass(), len);
		if (len > 0)
			arr[0] = oneState;
		for (int i = 1; i < len; i++)
			arr[i] = newState();
		return arr;
	}

	/**
	 * @param recordState
	 *            The state corresponding to this record
	 * @param record
	 *            A long representing the record
	 * @param toStore
	 *            The record to store the result in (as opposed to returning a
	 *            newly instantiated record)
	 */
	public abstract void longToRecord(S recordState, long record, Record toStore);

	/**
	 * A synchronized implementation of stateToString for JSONInterface (When
	 * solving, the game is cloned to ensure there are no synchronization
	 * problems)
	 * 
	 * @param recordState
	 *            The state corresponding to this record
	 * @param record
	 *            A long representing the record (extracted from a database)
	 * @param toStore
	 *            The record to store the result in (as opposed to returning a
	 *            newly instantiated record)
	 */
	public final synchronized void synchronizedLongToRecord(S recordState,
			long record, Record toStore) {
		longToRecord(recordState, record, toStore);
	}

	/**
	 * @param recordState
	 *            The state corresponding to this record
	 * @param fromRecord
	 *            The record to extract the long from
	 * @return A long representing the record (to be stored in a database)
	 */
	public abstract long recordToLong(S recordState, Record fromRecord);

	/**
	 * @return A new Record object (if you wish to subclass Record for your
	 *         game, you should over-ride this)
	 */
	public Record newRecord() {
		return new Record(conf);
	}

	/**
	 * @param recordArray
	 *            An array of records
	 * @return The record with the best possible outcome
	 */
	public Record combine(Record[] records) {
		Record best = records[0];
		for (int i = 1; i < records.length; i++) {
			if (records[i].isPreferableTo(best))
				best = records[i];
		}
		return best;
	}
}
