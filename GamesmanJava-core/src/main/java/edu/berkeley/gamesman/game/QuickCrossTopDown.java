package edu.berkeley.gamesman.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.Record;
import edu.berkeley.gamesman.core.State;
import edu.berkeley.gamesman.core.Value;
import edu.berkeley.gamesman.util.Pair;

/**
 * Top down Tic Tac Toe game by David modified for QuickCros
 */
public final class QuickCrossTopDown extends Game<QuickCrossTDState> {
	private final int width;
	private final int height;
	private final int boardSize;
	private final int piecesToWin;

	// private final long[] tierOffsets;
	// private final DartboardHasher dh;

	/**
	 * Default Constructor
	 * 
	 * @param conf
	 *            The Configuration object
	 */
	public QuickCrossTopDown(Configuration conf) {
		super(conf);
		width = conf.getInteger("gamesman.game.width", 4);
		height = conf.getInteger("gamesman.game.height", 4);
		boardSize = width * height;
		piecesToWin = conf.getInteger("gamesman.game.pieces", 4);
		// no longer needed
		/*
		 * tierOffsets = new long[boardSize + 2]; dh = new
		 * DartboardHasher(boardSize, ' ', 'O', 'X'); long total = 0; for (int i
		 * = 0; i <= boardSize; i++) { tierOffsets[i] = total;
		 * dh.setNums(boardSize - i, i / 2, (i + 1) / 2); total +=
		 * dh.numHashes(); } tierOffsets[boardSize + 1] = total;
		 */
	}

	@Override
	public Collection<QuickCrossTDState> startingPositions() {
		ArrayList<QuickCrossTDState> returnList = new ArrayList<QuickCrossTDState>(
				1);
		QuickCrossTDState returnState = newState();
		returnList.add(returnState);
		return returnList;
	}

	@Override
	// given state, returns groupings of names and child states
	public Collection<Pair<String, QuickCrossTDState>> validMoves(
			QuickCrossTDState pos) {

		// Below is for nonloopy game
		ArrayList<Pair<String, QuickCrossTDState>> moves = new ArrayList<Pair<String, QuickCrossTDState>>(
				boardSize - pos.numPieces);
		QuickCrossTDState[] children = new QuickCrossTDState[boardSize
				- pos.numPieces];

		// Below is for loopy game
		/*
		 * ArrayList<Pair<String, QuickCrossTDState>> moves = new
		 * ArrayList<Pair<String, QuickCrossTDState>>( pos.numPieces +
		 * 2*(boardSize - pos.numPieces)); QuickCrossTDState[] children = new
		 * QuickCrossTDState[pos.numPieces + 2 * (boardSize - pos.numPieces)];
		 */
		String[] childNames = new String[children.length];

		for (int i = 0; i < children.length; i++) {
			children[i] = newState();
		}

		// this fills up the children array
		validMoves(pos, children);
		int moveCount = 0;

		// below is for nonloopy

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (pos.getPiece(row, col) == ' ')
					childNames[moveCount++] = String
							.valueOf((char) ('A' + col))
							+ Integer.toString(row + 1);
			}
		}
		for (int i = 0; i < children.length; i++) {
			moves.add(new Pair<String, QuickCrossTDState>(childNames[i],
					children[i]));
		}
		return moves;

		// below is for loopy
		/*
		 * for (int row = 0; row < height; row++) { for (int col = 0; col <
		 * width; col++) { //in this case 2 possible moves if (pos.getPiece(row,
		 * col) == ' ') { childNames[moveCount++] = String .valueOf('H' + (char)
		 * ('A' + col)) + Integer.toString(row + 1); childNames[moveCount++] =
		 * String .valueOf('V' + (char) ('A' + col)) + Integer.toString(row +
		 * 1); } if (pos.getPiece(row, col) == '-' || pos.getPiece(row, col) ==
		 * '|'){ childNames[moveCount++] = String .valueOf('F' + (char) ('A' +
		 * col)) + Integer.toString(row + 1); } } } for (int i = 0; i <
		 * children.length; i++) { moves.add(new Pair<String,
		 * QuickCrossTDState>(childNames[i], children[i])); } return moves;
		 */
	}

	@Override
	public int maxChildren() {
		// nonloopy
		return boardSize;
		// loopy
		// return boardSize*2;
	}

	@Override
	public String stateToString(QuickCrossTDState pos) {
		return pos.toString();
	}

	@Override
	public String displayState(QuickCrossTDState pos) {
		StringBuilder sb = new StringBuilder((width + 1) * 2 * (height + 1));
		for (int row = height - 1; row >= 0; row--) {
			sb.append(row + 1);
			for (int col = 0; col < width; col++) {
				sb.append(" ");
				char piece = pos.getPiece(row, col);
				if (piece == ' ')
					sb.append(' ');
				else if (piece == '-' || piece == '|')
					sb.append(piece);
				else
					throw new Error(piece + " is not a valid piece");
			}
			sb.append("\n");
		}
		sb.append(" ");
		for (int col = 0; col < width; col++) {
			sb.append(" ");
			sb.append((char) ('A' + col));
		}
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public QuickCrossTDState stringToState(String pos) {
		return new QuickCrossTDState(width, pos.toCharArray());
	}

	@Override
	public String describe() {
		return width + "x" + height + " QuickCross with " + piecesToWin
				+ " pieces";
	}

	@Override
	public QuickCrossTDState newState() {
		return new QuickCrossTDState(width, height);
	}

	@Override
	public int validMoves(QuickCrossTDState pos, QuickCrossTDState[] children) {

		int numMoves = 0;
		// nonloopy

		for (int i = 0; i < boardSize; i++) {
			if (pos.getPiece(i) == ' ') {
				children[numMoves].set(pos);
				children[numMoves].setPiece(i, '-');
				numMoves++;
			}
		}
		return numMoves;

		// loopy
		/*
		 * for (int i = 0; i < (pos.numPieces + 2 * (boardSize -
		 * pos.numPieces)); i++){ if (pos.getPiece(i) == ' '){
		 * children[numMoves].set(pos); children[numMoves].setPiece(i, '-');
		 * numMoves++; children[numMoves].set(pos);
		 * children[numMoves].setPiece(i, '|'); numMoves++; } else if
		 * (pos.getPiece(i) == '-'){ children[numMoves].set(pos);
		 * children[numMoves].setPiece(i, '|'); numMoves++; } else if
		 * (pos.getPiece(i) == '|'){ children[numMoves].set(pos);
		 * children[numMoves].setPiece(i, '-'); numMoves++; } else throw new
		 * Error("cannot generate valid moves from given pos"); } return
		 * numMoves;
		 */
	}

	@Override
	public Value primitiveValue(QuickCrossTDState pos) {
		// char lastTurn = pos.numPieces % 2 == 0 ? 'O' : 'X';

		// if last move was 1st player and currently even num moves have
		// happened, 4 in a row is a win for me (the 2nd player)
		Value WinorLose = (pos.lastMoveOne == pos.evenNumMoves ? Value.WIN
				: Value.LOSE);

		char currPiece = '-';
		// try both pieces
		for (int i = 0; i < 2; i++) {
			// checks for a vertical win
			for (int row = 0; row < height; row++) {
				int piecesInRow = 0;
				for (int col = 0; col < width; col++) {
					if (pos.getPiece(row, col) == currPiece) {
						piecesInRow++;
						if (piecesInRow == piecesToWin)
							return WinorLose;
					} else
						piecesInRow = 0;
				}
			}

			// checks for a horizontal win
			for (int col = 0; col < width; col++) {
				int piecesInCol = 0;
				for (int row = 0; row < height; row++) {
					if (pos.getPiece(row, col) == currPiece) {
						piecesInCol++;
						if (piecesInCol == piecesToWin)
							return WinorLose;
					} else
						piecesInCol = 0;
				}
			}
			// checks for diagonal win /
			for (int row = 0; row <= height - piecesToWin; row++) {
				for (int col = 0; col <= width - piecesToWin; col++) {
					int pieces;
					for (pieces = 0; pieces < piecesToWin; pieces++) {
						if (pos.getPiece(row + pieces, col + pieces) != currPiece)
							break;
					}
					if (pieces == piecesToWin)
						return WinorLose;
				}
			}
			// checks for diagonal win \
			for (int row = 0; row <= height - piecesToWin; row++) {
				for (int col = piecesToWin - 1; col < width; col++) {
					int pieces;
					for (pieces = 0; pieces < piecesToWin; pieces++) {
						if (pos.getPiece(row + pieces, col - pieces) != currPiece)
							break;
					}
					if (pieces == piecesToWin)
						return WinorLose;
				}
			}
			currPiece = '|';
		}
		return Value.UNDECIDED;
	}

	@Override
	// trinary hash + 2 bits at end for lastMoveOne and evenNumMoves
	public long stateToHash(QuickCrossTDState pos) {
		// long offset = tierOffsets[pos.numPieces];
		// return offset + dh.setNumsAndHash(pos.board);
		long retHash = 0;

		int index = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (pos.getPiece(x, y) == ' ') {
					// no change
				} else if (pos.getPiece(x, y) == '-') {
					retHash += Math.pow(3, index);
				} else if (pos.getPiece(x, y) == '|') {
					retHash += Math.pow(3, index) * 2;
				} else
					throw new Error("Error when hashing, bad piece");
				index++;
			}
		}
		// retHash = retHash << 1;
		// if (pos.evenNumMoves){
		// retHash = retHash + 1;
		// }
		return retHash;
	}

	@Override
	public long numHashes() {
		// return tierOffsets[boardSize + 1];
		return (long) Math.pow(3, boardSize) << 1;
	}

	// i have no idea what this does
	@Override
	public long recordStates() {
		return boardSize + 3;
	}

	// seems fishy that the only thing changed is the numPieces...
	@Override
	public void hashToState(long hash, QuickCrossTDState s) {
		/*
		 * int tier = Arrays.binarySearch(tierOffsets, hash); if (tier < 0) tier
		 * = -tier - 2; hash -= tierOffsets[tier]; dh.setNums(boardSize - tier,
		 * tier / 2, (tier + 1) / 2); dh.unhash(hash); dh.getCharArray(s.board);
		 * s.numPieces = tier;
		 */

		s.numPieces = 0;
		long hashLeft = hash;
		for (int index = width * height - 1; index >= 0; index--) {
			int y = index / width;
			int x = index % width;
			double base = Math.pow(3, index);
			if (hashLeft < base) {
				s.setPiece(x, y, ' ');
			} else if (hashLeft < base * 2) {
				s.setPiece(x, y, '-');
				s.numPieces++;
				hashLeft = (long) (hashLeft - base);
			} else if (hashLeft >= base * 2) {
				s.setPiece(x, y, '|');
				s.numPieces++;
				hashLeft = (long) (hashLeft - (base * 2));
			}
		}
		// s.evenNumMoves = false;
		// if (hash % 2 == 1){
		// s.evenNumMoves = true;
		// }
	}

	@Override
	public void longToRecord(QuickCrossTDState recordState, long record,
			Record toStore) {
		if (record == boardSize + 1) {
			toStore.value = Value.TIE;
			toStore.remoteness = boardSize - recordState.numPieces;
		} else if (record == boardSize + 2)
			toStore.value = Value.UNDECIDED;
		else if (record >= 0 && record <= boardSize) {
			// toStore.value = record % 2 == 0 ? Value.LOSE : Value.WIN;
			toStore.value = (record & 1) == 1 ? Value.WIN : Value.LOSE;
			toStore.remoteness = (int) record;
		}
	}

	@Override
	// stuff needs to be fixed here
	public long recordToLong(QuickCrossTDState recordState, Record fromRecord) {
		if (fromRecord.value == Value.WIN || fromRecord.value == Value.LOSE)
			return fromRecord.remoteness;
		else if (fromRecord.value == Value.TIE)
			return boardSize + 1;
		else if (fromRecord.value == Value.UNDECIDED)
			return boardSize + 2;
		else
			throw new Error("Invalid Value");
	}
}

// current state of the board
class QuickCrossTDState implements State<QuickCrossTDState> {
	final char[] board;
	private final int width;
	int numPieces = 0;

	// previous move was made by first player
	boolean lastMoveOne = false;
	// even number of moves so far
	boolean evenNumMoves = true;

	public QuickCrossTDState(int width, int height) {
		this.width = width;
		board = new char[width * height];
		for (int i = 0; i < board.length; i++) {
			board[i] = ' ';
		}
	}

	public QuickCrossTDState(int width, char[] charArray) {
		this.width = width;
		board = charArray;
	}

	@Override
	public void set(QuickCrossTDState qcs) {
		if (board.length != qcs.board.length)
			throw new Error("Different Length Boards");
		int boardLength = board.length;
		System.arraycopy(qcs.board, 0, board, 0, boardLength);
		numPieces = qcs.numPieces;
		lastMoveOne = qcs.lastMoveOne;
		evenNumMoves = qcs.evenNumMoves;
	}

	public void setPiece(int row, int col, char piece) {
		setPiece(row * width + col, piece);
	}

	public void setPiece(int index, char piece) {
		if (board[index] == ' ') {
			board[index] = piece;
			numPieces++;
			lastMoveOne = !lastMoveOne;
			evenNumMoves = !evenNumMoves;
		} else if (board[index] == '-' || board[index] == '|') {
			board[index] = piece;
		} else
			throw new Error("Invalid board when setting piece");
	}

	/*
	 * ASK DAVID ABOUT NUMPIECES public void setPiece(int index, char piece) {
	 * //fail case if (board[index] != ' '){ numPieces--; }
	 * 
	 * board[index] = piece;
	 * 
	 * //good if (piece == 'X' || piece == 'O') { numPieces++; }
	 * 
	 * //bad else if (piece != ' ') throw new Error("Invalid piece: " + piece);
	 * }
	 */

	public char getPiece(int row, int col) {
		return getPiece(row * width + col);
	}

	public char getPiece(int index) {
		return board[index];
	}

	public String toString() {
		return Arrays.toString(board);
	}
}
