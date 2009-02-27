package edu.berkeley.gamesman.game;

import java.math.BigInteger;

import edu.berkeley.gamesman.util.Pair;

/**
 * @author DNSpies
 * Convenience class for FastConnect4
 */
public class IteratorState extends Pair<Integer, BigInteger> {

	/**
	 * @param tier The tier
	 * @param hash The hash
	 */
	public IteratorState(Integer tier, BigInteger hash) {
		super(tier, hash);
	}

	/**
	 * @return The tier
	 */
	public int tier() {
		return car;
	}

	/**
	 * @return The hash
	 */
	public BigInteger hash() {
		return cdr;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5037120076970031108L;

}