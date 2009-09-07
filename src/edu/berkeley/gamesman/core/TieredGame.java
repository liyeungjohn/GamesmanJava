package edu.berkeley.gamesman.core;

import java.util.Iterator;

import edu.berkeley.gamesman.util.Pair;
import edu.berkeley.gamesman.util.Util;

/**
 * Superclass of Tiered games. Each game state falls into a logical tier. As an
 * example, you can represent TicTacToe as a tiered game with the tier being the
 * number of pieces placed.
 * 
 * The important invariant is that any board's value must depend only on (a)
 * primitives or (b) boards in a later tier. This allows us to solve from the
 * last tier up to the top at tier 0 (the starting state) in a very efficient
 * manner
 * 
 * @author Steven Schlansker
 * 
 * @param <State> The type that you use to represent your States
 */
public abstract class TieredGame<State> extends Game<State> {
	protected TieredHasher<State> myHasher;

	/**
	 * Default constructor
	 * 
	 * @param conf the configuration
	 */
	public TieredGame(Configuration conf) {
		super(conf);
	}

	@Override
	public void prepare() {
		myHasher = Util.checkedCast(conf.getHasher());
	}

	@Override
	public State hashToState(long hash) {
		if (myHasher == null)
			Util.fatalError("You must call prepare() before hashing!");
		if (myHasher.cacheNumTiers == -1)
			myHasher.cacheNumTiers = myHasher.numberOfTiers();
		if (myHasher.tierEnds == null)
			myHasher.lastHashValueForTier(myHasher.cacheNumTiers - 1);

		for (int i = 0; i < myHasher.cacheNumTiers; i++) {
			if (myHasher.tierEnds[i] >= hash) {
				if (i == 0)
					return myHasher.gameStateForTierAndOffset(i, hash);
				return myHasher.gameStateForTierAndOffset(i, hash
						- myHasher.tierEnds[i - 1] - 1);
			}
		}
		Util.fatalError("Hash outside of tiered values: " + hash);
		return null;
	}

	@Override
	public long stateToHash(State pos) {
		if (myHasher == null)
			Util.fatalError("You must call prepare() before hashing!");
		Pair<Integer, Long> p = myHasher.tierIndexForState(pos);
		return myHasher.hashOffsetForTier(p.car)+p.cdr;
	}

	/**
	 * @return the number of Tiers in this particular game
	 */
	public int numberOfTiers() {
		if (myHasher == null)
			Util.fatalError("You must call prepare() before hashing!");
		return myHasher.numberOfTiers();
	}

	/**
	 * @param tier the Tier we're interested in
	 * @return the first hash value for that tier
	 */
	public final long hashOffsetForTier(int tier) {
		if (myHasher == null)
			Util.fatalError("You must call prepare() before hashing!");
		return myHasher.hashOffsetForTier(tier);
	}

	/**
	 * @param tier the Tier we're interested in
	 * @return the last hash value that is still within that tier
	 */
	public final long lastHashValueForTier(int tier) {
		if (myHasher == null)
			Util.fatalError("You must call prepare() before hashing!");
		return myHasher.lastHashValueForTier(tier);
	}

	@Override
	public final long lastHash() {
		return lastHashValueForTier(numberOfTiers() - 1);
	}

	public Iterator<Integer> tierDependsOn(final int tier) {
		if (tier >= numberOfTiers() - 1)
			return new Iterator<Integer>() {
				public boolean hasNext() {
					return false;
				}

				public Integer next() {
					return null;
				}

				public void remove() {
				}
			};
		return new Iterator<Integer>() {
			boolean seen = false;

			public boolean hasNext() {
				return !seen;
			}

			public Integer next() {
				seen = true;
				return tier + 1;
			}

			public void remove() {
			}
		};
	}
}
