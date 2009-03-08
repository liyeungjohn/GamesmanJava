package edu.berkeley.gamesman.hasher;

import java.math.BigInteger;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.TieredHasher;
import edu.berkeley.gamesman.game.OneTwoN;
import edu.berkeley.gamesman.util.Pair;
import edu.berkeley.gamesman.util.Util;

/**
 * @author Wesley Hart
 *
 */
public class OneTwoNHasher extends TieredHasher<Integer> {
	private final OneTwoN game;
	/**
	 * Default constructor
	 * @param conf the configuration
	 */
	public OneTwoNHasher(Configuration conf) {
		super(conf);
		if(!(conf.getGame() instanceof OneTwoN))
			Util.fatalError("This hasher is only designed for OneTwoN");
		game = (OneTwoN) conf.getGame();
	}

	@Override
	public BigInteger numHashesForTier(int tier) {
		return BigInteger.ONE;
	}

	@Override
	public Integer gameStateForTierAndOffset(int tier, BigInteger index) {
		Util.assertTrue(index.equals(BigInteger.ZERO), "Index must always be 0");
		return tier;
	}

	@Override
	public int numberOfTiers() {
		return game.maxNumber+1;
	}
	
	@Override
	public Pair<Integer, BigInteger> tierIndexForState(Integer state) {
		return new Pair<Integer, BigInteger>(state,BigInteger.ZERO);
	}

	@Override
	public String describe() {
		return "OTNH";
	}
}
