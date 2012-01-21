package edu.berkeley.gamesman.parallel.game.connect4;

import java.util.Arrays;

import edu.berkeley.gamesman.hasher.DBHasher;
import edu.berkeley.gamesman.hasher.genhasher.DBInvCalculator;
import edu.berkeley.gamesman.hasher.genhasher.GenHasher;
import edu.berkeley.gamesman.hasher.invhasher.OptimizingInvariantHasher;

public class C4Hasher extends OptimizingInvariantHasher<C4State> {
	private final int width, height, boardSize;
	private final DBInvCalculator calc;

	public C4Hasher(int width, int height) {
		super(makeDigitBase(width, height));
		this.width = width;
		this.height = height;
		boardSize = width * height;
		calc = new DBInvCalculator(boardSize);
	}

	private static int[] makeDigitBase(int width, int height) {
		int boardSize = width * height;
		int[] digitBase = new int[boardSize + 1];
		Arrays.fill(digitBase, 3);
		digitBase[boardSize] = boardSize + 1;
		return digitBase;
	}

	@Override
	protected long getInvariant(C4State state) {
		int start = getStart(state);
		if (start == boardSize + 1)
			return 0;
		else if (start == boardSize)
			return state.get(boardSize);
		boolean startEmpty = leastSig(state) == 0;
		if (!isTop(start) && startEmpty && state.get(start + 1) != 0)
			return -1;
		else {
			return calc.getInv(state) + (startEmpty ? 0 : calc.numValues);
		}
	}

	private boolean isTop(int place) {
		return (place + 1) % height == 0;
	}

	@Override
	protected boolean valid(C4State state) {
		return getInvariant(state) >= 0 && DBHasher.dbValid(state, boardSize);
	}

	@Override
	protected C4State innerNewState() {
		return new C4State(this, width, height);
	}

	public static void main(String[] args) {
		GenHasher.enableToughAsserts();
		C4Hasher dbh = new C4Hasher(3, 2);
		C4State s = dbh.newState();
		do {
			System.out.println(dbh.hash(s));
			System.out.println(dbh.printBoard(s));
		} while (dbh.step(s) != -1);
	}

	public String printBoard(C4State s) {
		StringBuilder sb = new StringBuilder();
		for (int row = height - 1; row >= 0; row--) {
			sb.append("|");
			for (int col = 0; col < width; col++) {
				sb.append(getChar(s.get(col * height + row)));
				sb.append("|");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private static char getChar(int c) {
		switch (c) {
		case 0:
			return ' ';
		case 1:
			return 'X';
		case 2:
			return 'O';
		default:
			return '?';
		}
	}
}