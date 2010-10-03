package edu.berkeley.gamesman.game;

import java.util.ArrayList;
import java.util.Collection;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.Value;
import edu.berkeley.gamesman.core.Record;
import edu.berkeley.gamesman.core.State;
import edu.berkeley.gamesman.util.Pair;
import edu.berkeley.gamesman.util.qll.Factory;
import edu.berkeley.gamesman.util.qll.RLLFactory;
import edu.berkeley.gamesman.util.qll.RecycleLinkedList;

/**
 * A wrapper for using the top-down solver with any game
 * 
 * @author dnspies
 * 
 * @param <S>
 *            The game state
 */
public final class TopDownGame<S extends State> extends TopDownMutaGame {
	private final Game<S> myGame;
	private final RecycleLinkedList<RecycleLinkedList<S>> moveLists;
	private final RecycleLinkedList<S> stateList;
	private final S[] possibleMoves;
	private final S[] startingPositions;

	/**
	 * @param g
	 *            The game to wrap
	 */
	public TopDownGame(Configuration conf, Game<S> g) {
		super(conf);
		myGame = g;
		moveLists = new RecycleLinkedList<RecycleLinkedList<S>>(
				new Factory<RecycleLinkedList<S>>() {
					RLLFactory<S> gen = new RLLFactory<S>(new Factory<S>() {

						public S newObject() {
							return myGame.newState();
						}

						public void reset(S t) {
						}
					});

					public RecycleLinkedList<S> newObject() {
						return gen.getList();
					}

					public void reset(RecycleLinkedList<S> t) {
						t.clear();
					}

				});
		stateList = new RecycleLinkedList<S>(new Factory<S>() {

			public S newObject() {
				return myGame.newState();
			}

			public void reset(S t) {
			}

		});
		stateList.add();
		possibleMoves = myGame.newStateArray(myGame.maxChildren());
		Collection<S> startingPositions = myGame.startingPositions();
		this.startingPositions = startingPositions.toArray(myGame
				.newStateArray(startingPositions.size()));
	}

	@Override
	public boolean changeMove() {
		if (moveLists.getLast().isEmpty())
			return false;
		S m = moveLists.getLast().removeFirst();
		stateList.getLast().set(m);
		return true;
	}

	@Override
	public String displayState() {
		return myGame.displayState(stateList.getLast());
	}

	@Override
	public long getHash() {
		return myGame.stateToHash(stateList.getLast());
	}

	@Override
	public boolean makeMove() {
		RecycleLinkedList<S> moves = moveLists.addLast();
		int numMoves = myGame.validMoves(stateList.getLast(), possibleMoves);
		if (numMoves == 0) {
			moveLists.removeLast();
			return false;
		} else {
			for (int i = 0; i < numMoves; i++) {
				S move = moves.add();
				move.set(possibleMoves[i]);
			}
			S curState = stateList.addLast();
			curState.set(moves.removeFirst());
			return true;
		}
	}

	@Override
	public Value primitiveValue() {
		return myGame.primitiveValue(stateList.getLast());
	}

	@Override
	public void setFromString(String pos) {
		setToState(myGame.stringToState(pos));
	}

	@Override
	public void setToHash(long hash) {
		stateList.clear();
		moveLists.clear();
		S state = stateList.add();
		myGame.hashToState(hash, state);
	}

	private void setToState(S pos) {
		stateList.clear();
		moveLists.clear();
		S state = stateList.add();
		state.set(pos);
	}

	@Override
	public void undoMove() {
		moveLists.removeLast();
		stateList.removeLast();
	}

	@Override
	public Collection<String> moveNames() {
		Collection<Pair<String, S>> validMoves = myGame.validMoves(stateList
				.getLast());
		ArrayList<String> moveNames = new ArrayList<String>(validMoves.size());
		for (Pair<String, S> move : validMoves) {
			moveNames.add(move.car);
		}
		return moveNames;
	}

	@Override
	public int numStartingPositions() {
		return startingPositions.length;
	}

	@Override
	public void setStartingPosition(int i) {
		setToState(startingPositions[i]);
	}

	@Override
	public void longToRecord(long record, Record toStore) {
		myGame.longToRecord(stateList.getLast(), record, toStore);
	}

	@Override
	public long recordToLong(Record fromRecord) {
		return myGame.recordToLong(stateList.getLast(), fromRecord);
	}

	@Override
	public int maxChildren() {
		return myGame.maxChildren();
	}

	@Override
	public String describe() {
		return myGame.describe();
	}

	@Override
	public long numHashes() {
		return myGame.numHashes();
	}

	@Override
	public long recordStates() {
		return myGame.recordStates();
	}
}
