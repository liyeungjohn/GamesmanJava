package edu.berkeley.gamesman.parallel.game.connections;

import java.util.Arrays;

import edu.berkeley.gamesman.hasher.invhasher.OptimizingInvariantHasher;
import edu.berkeley.gamesman.hasher.counting.CountingState;
import edu.berkeley.gamesman.hasher.genhasher.DBInvCalculator;

public class ConnectionsHasher extends OptimizingInvariantHasher<CountingState> {

	/*
	 * this class exists b/c not every player can place in any of the edges, so
	 * this game is not strictly a dartboard game. override getInv to return -1
	 * if the wrong player's piece is in one of the edges b/c in order to
	 * surround, players should be able to place in edges
	 */
	private final int boardSize;
	private final DBInvCalculator calc;

	/**
	 * @param numElements
	 * @param digitBase
	 * @param numPieces
	 */
	public ConnectionsHasher(int boardSize) {
		super(makeParams(boardSize));
		this.boardSize = boardSize;
		calc = new DBInvCalculator(boardSize);
	}

	private static int[] makeParams(int boardSize) {
		int[] result = new int[boardSize + 1];
		Arrays.fill(result, 3);
		result[boardSize] = boardSize + 1;
		return result;
	}

	@Override
	protected CountingState genHasherNewState() {
		return new CountingState(this, boardSize);
	}

	// We changed this: it will not let you play an illegal move.
	protected long getInvariant(CountingState state) { 
		if (state.get(0) == 2 || state.get(1) == 2 || state.get(19) == 2
				|| state.get(20) == 2) {
			return -1;
		} else if (state.get(5) == 1 || state.get(8) == 1 || state.get(9) == 1
				|| state.get(12) == 1) {
			return -1;
		} else {
			return calc.getInv(state);
		}
	}

	@Override
	protected boolean valid(CountingState state) {
		return dbValid(state, boardSize);
	}

	public static boolean dbValid(CountingState state, int boardSize) {
		int num1 = state.numPieces(1);
		int num2 = state.numPieces(2);
		return num1 + num2 == state.get(boardSize)
				&& (num1 - num2 == 1 || num1 - num2 == 0);
	}

}
