package edu.berkeley.gamesman.game;

import java.util.ArrayList;
import java.util.Collection;

import edu.berkeley.gamesman.core.*;
import edu.berkeley.gamesman.game.util.TierState;
import edu.berkeley.gamesman.hasher.MMHasher;
import edu.berkeley.gamesman.util.DebugFacility;
import edu.berkeley.gamesman.util.Pair;
import edu.berkeley.gamesman.util.Util;

/**
 * A superclass for hex-style dartboard games in which the objective is to
 * connect across the board somehow
 * 
 * @author dnspies
 */
public abstract class ConnectGame extends TierGame {
	private char turn;
	protected final MMHasher mmh;
	protected final TierState myState = newState();

	/**
	 * @param conf
	 *            The configuration object
	 */
	public ConnectGame(Configuration conf) {
		super(conf);
		mmh = new MMHasher();
	}

	protected abstract int getBoardSize();

	@Override
	public TierState getState() {
		return myState;
	}

	@Override
	public int getTier() {
		return myState.tier;
	}

	@Override
	public boolean hasNextHashInTier() {
		return myState.hash < numHashesForTier() - 1;
	}

	@Override
	public int maxChildren() {
		return getBoardSize();
	}

	@Override
	public void nextHashInTier() {
		myState.hash++;
		gameMatchState();
	}

	@Override
	public long numHashesForTier() {
		int tier = getTier();
		return Util.nCr(getBoardSize(), tier) * Util.nCr(tier, tier / 2);
	}

	@Override
	public long numHashesForTier(int tier) {
		return Util.nCr(getBoardSize(), tier) * Util.nCr(tier, tier / 2);
	}

	@Override
	public int numStartingPositions() {
		return 1;
	}

	@Override
	public int numberOfTiers() {
		return getBoardSize() + 1;
	}

	@Override
	public void setFromString(String pos) {
		setToCharArray(convertInString(pos));
		stateMatchGame();
	}

	private void stateMatchGame() {
		char[] arr = getCharArray();
		int tier = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != ' ')
				tier++;
		}
		myState.tier = tier;
		turn = ((tier & 1) == 1) ? 'O' : 'X';
		myState.hash = mmh.hash(getCharArray());
	}

	private void gameMatchState() {
		int tier = myState.tier;
		turn = ((tier & 1) == 1) ? 'O' : 'X';
		mmh.unhash(myState.hash, getCharArray(), (tier + 1) / 2, tier / 2);
	}

	@Override
	public void setStartingPosition(int n) {
		char[] arr = getCharArray();
		int size = getBoardSize();
		for (int i = 0; i < size; i++)
			arr[i] = ' ';
		setToCharArray(arr);
		myState.tier = 0;
		turn = 'X';
		myState.hash = 0;
	}

	@Override
	public void setState(TierState pos) {
		myState.set(pos);
		gameMatchState();
	}

	@Override
	public void setTier(int tier) {
		myState.tier = tier;
		myState.hash = 0;
		gameMatchState();
	}

	@Override
	public Collection<Pair<String, TierState>> validMoves() {
		char turn = this.turn;
		char[] pieces = getCharArray();
		ArrayList<Pair<String, TierState>> moves = new ArrayList<Pair<String, TierState>>(
				pieces.length);
		for (int i = 0; i < pieces.length; i++) {
			if (pieces[i] == ' ') {
				pieces[i] = turn;
				stateMatchGame();
				moves.add(new Pair<String, TierState>(Integer
						.toString(translateOut(i)), myState.clone()));
				pieces[i] = ' ';
			}
		}
		stateMatchGame();
		return moves;
	}

	/**
	 * @param i
	 *            The index into the char array
	 * @return The index into the passed game string.
	 */
	public int translateOut(int i) {
		return i;
	}

	@Override
	public int validMoves(TierState[] moves) {
		char turn = this.turn;
		char[] pieces = getCharArray();
		int c = 0;
		for (int i = 0; i < pieces.length; i++) {
			if (pieces[i] == ' ') {
				pieces[i] = turn;
				stateMatchGame();
				moves[c].set(myState);
				pieces[i] = ' ';
				c++;
			}
		}
		stateMatchGame();
		return c;
	}

	@Override
	public long recordStates() {
		if (conf.remotenessStates > 0)
			return getBoardSize() + 1;
		else
			return 2;
	}

	protected abstract char[] getCharArray();

	@Override
	public String stateToString() {
		return convertOutString(getCharArray());
	}

	public char[] convertInString(String s) {
		return s.toCharArray();
	}

	public String convertOutString(char[] charArray) {
		return new String(charArray);
	}

	protected abstract void setToCharArray(char[] myPieces);

	@Override
	public Value primitiveValue() {
		Value result;
		if ((myState.tier & 1) == 1)
			result = isWin('X') ? Value.LOSE
					: Value.UNDECIDED;
		else
			result = isWin('O') ? Value.LOSE
					: Value.UNDECIDED;
		assert Util.debug(DebugFacility.GAME, result.name() + "\n");
		if (myState.tier == numberOfTiers() - 1
				&& result == Value.UNDECIDED)
			return Value.IMPOSSIBLE;
		else
			return result;
	}

	@Override
	public long getRecord(TierState recordState, Record fromRecord) {
		if (conf.remotenessStates > 0) {
			return fromRecord.remoteness;
		} else {
			switch (fromRecord.value) {
			case LOSE:
				return 0L;
			case WIN:
				return 1L;
			default:
				return 0L;
			}
		}
	}

	@Override
	public void recordFromLong(TierState recordState, long state, Record toStore) {
		if ((state & 1) == 1)
			toStore.value = Value.WIN;
		else
			toStore.value = Value.LOSE;
		if (conf.remotenessStates > 0)
			toStore.remoteness = (int) state;
	}

	protected abstract boolean isWin(char c);
}