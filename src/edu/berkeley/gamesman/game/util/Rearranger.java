package edu.berkeley.gamesman.game.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import edu.berkeley.gamesman.util.Pair;


/**
 * <p>
 * Given a string of X's, O's, and spaces, iterates over all rearrangements of
 * X's and O's and finds all hash values when X's/O's are inserted in the
 * spaces.
 * </p>
 * <p>
 * This class is most ideally suited for tiered games such as Tic Tac Toe or
 * hex, in which there are a set number of spaces; all of which are available
 * until a player plays in them. For instance, a tic tac toe board might be
 * passed as such: ("T   T   T",2,1). The board will be initialized to:<br />
 * <code>
 * |O--|<br />
 * |-O-|<br />
 * |--X|<br />
 * </code> and iterate over<br />
 * <code>
 * |O--|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|X--|<br />
 * |-X-| and |-O-|<br />
 * |--O|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|--O|<br />
 * </code>
 * </p>
 * <p>
 * In each scenario, getChildren('X') will return 6 pairs; one for each of x's 6
 * possible moves at positions 2, 3, 4, 6, 7, and 8
 * </p>
 * <p>
 * While this is not ideally suited to Connect Four, it's not too difficult to
 * implement. Reduce all space above an unfilled column to just one and then
 * implement the necessary math in the caller to determine which position is
 * which
 * </p>
 * 
 * @author DNSpies
 */
public final class Rearranger implements Cloneable{
	private static final class HashPiece {
		final int index;
		int os;
		char player;
		BigInteger hash;
		BigInteger nextO;
		BigInteger nextX;
		HashGroup group;

		protected HashPiece(int pieces, int os, BigInteger hash, char player,
				HashGroup group) {
			this.player = player;
			this.index = pieces;
			this.os = os;
			this.hash = hash;
			if (hash.equals(BigInteger.ZERO)) {
				nextO = BigInteger.ZERO;
				nextX = BigInteger.ONE;
			} else {
				nextO = hash.multiply(BigInteger.valueOf(pieces + 1)).divide(
						BigInteger.valueOf(os + 1));
				nextX = hash.multiply(BigInteger.valueOf(pieces + 1)).divide(
						BigInteger.valueOf(pieces + 1 - os));
			}
			this.group = group;
		}

		protected void set(int os, BigInteger hash, char player) {
			BigInteger hashChange = BigInteger.ZERO;
			BigInteger xChange = BigInteger.ZERO;
			BigInteger oChange = BigInteger.ZERO;
			if (this.player == 'O') {
				hashChange = hashChange.subtract(this.hash);
				xChange = hashChange.subtract(nextX);
				oChange = oChange.subtract(nextO);
			}
			this.player = player;
			this.os = os;
			this.hash = hash;
			if (hash.equals(BigInteger.ZERO)) {
				this.nextO = BigInteger.ZERO;
				this.nextX = BigInteger.ONE;
			} else {
				this.nextO = hash.multiply(BigInteger.valueOf(index + 1))
						.divide(BigInteger.valueOf(os + 1));
				this.nextX = hash.multiply(BigInteger.valueOf(index + 1))
						.divide(BigInteger.valueOf(index + 1 - os));
			}
			if (player == 'O') {
				hashChange = hashChange.add(hash);
				xChange = xChange.add(nextX);
				oChange = oChange.add(nextO);
			}
			group.setPiece(hashChange, oChange, xChange);
		}

		public String toString() {
			return String.valueOf(player);
		}
	}

	private static final class HashGroup {

		private final HashPiece[] pieces;
		final int index;
		final int empty;
		final HashPiece lastPiece;
		BigInteger groupHash = BigInteger.ZERO;
		BigInteger addO = BigInteger.ZERO;
		BigInteger addX = BigInteger.ZERO;

		private HashGroup(char[] s, HashGroup lastGroup,
				HashPiece[] pieceArray, int index) throws Exception {
			pieces = new HashPiece[s.length];
			this.index = index;
			HashPiece lastPiece;
			if (lastGroup == null)
				lastPiece = new HashPiece(-1, 0, BigInteger.ZERO, 'X', this);
			else
				lastPiece = lastGroup.lastPiece;
			for (int i = 0; i < s.length; i++) {
				if (s[i] == 'O') {
					pieces[i] = new HashPiece(lastPiece.index + 1,
							lastPiece.os + 1, lastPiece.nextO, 'O', this);
					addO = addO.add(pieces[i].nextO.subtract(pieces[i].hash));
					addX = addX.add(pieces[i].nextX.subtract(pieces[i].hash));
					groupHash = groupHash.add(pieces[i].hash);
				} else if (s[i] == 'X')
					pieces[i] = new HashPiece(lastPiece.index + 1, lastPiece.os,
							lastPiece.nextX, 'X', this);
				else
					throw new Exception("Bad character: " + s[i]);
				lastPiece = pieces[i];
				pieceArray[index++] = lastPiece;
			}
			this.empty = index;
			this.lastPiece = lastPiece;
		}

		private void setPiece(BigInteger hashChange, BigInteger addOChange,
				BigInteger addXChange) {
			groupHash = groupHash.add(hashChange);
			addO = addO.add(addOChange);
			addX = addX.add(addXChange);
		}

		protected HashPiece getPiece(int piece) {
			return pieces[piece];
		}

		public int size() {
			return pieces.length;
		}
	}

	/**
	 * The number of possible arrangements of the given number of X's and O's
	 */
	public final BigInteger arrangements;
	private final HashGroup[] groups;
	private final int numPieces, numOs;
	private BigInteger hash = BigInteger.ZERO;
	private int openX = 0, openO = 0;
	private boolean hasNext = true;
	private final HashPiece[] pieces;

	/**
	 * @param s A character representation of the board (in 'X' 'O' and ' ')
	 * @throws Exception If the char array contains other characters
	 */
	public Rearranger(final char[] s) throws Exception {
		LinkedList<HashGroup> g = new LinkedList<HashGroup>();
		HashGroup lastGroup;
		HashGroup pg = null;
		pieces = new HashPiece[s.length];
		int index = 0;
		int numPieces = 0, numOs = 0;
		String thisGroup = "";
		boolean onFX = true;
		boolean onFO = true;
		for (int i = 0; i < s.length; i++) {
			if (s[i] == ' ') {
				lastGroup = pg;
				pg = new HashGroup(thisGroup.toCharArray(), lastGroup, pieces,
						index);
				index = pg.empty + 1;
				hash = hash.add(pg.groupHash);
				g.add(pg);
				thisGroup = "";
			} else {
				if (onFX && s[i] == 'X')
					openX++;
				else if (onFO) {
					onFX = false;
					if (s[i] == 'O')
						openO++;
					else
						onFO = false;
				}
				if (s[i] == 'O')
					numOs++;
				thisGroup += s[i];
				numPieces++;
			}
		}
		lastGroup = pg;
		pg = new HashGroup(thisGroup.toCharArray(), lastGroup, pieces, index);
		index = pg.empty + 1;
		hash = hash.add(pg.groupHash);
		g.add(pg);
		thisGroup = "";
		if (onFO)
			hasNext = false;
		arrangements = pg.lastPiece.nextX;
		groups = g.toArray(new HashGroup[g.size()]);
		this.numPieces = numPieces;
		this.numOs = numOs;
	}

	/**
	 * @param s A character outline telling where the pieces and spaces are
	 * @param os The number of O's on the board
	 * @param xs The number of X's on the board
	 * @throws Exception If the number of pieces in the outline is not os + xs
	 */
	public Rearranger(final char[] s, int os, int xs) throws Exception {
		LinkedList<HashGroup> g = new LinkedList<HashGroup>();
		numPieces = xs + os;
		numOs = os;
		openO = os;
		openX = 0;
		if(numPieces<=1)
			hasNext=false;
		HashGroup lastGroup;
		HashGroup pg = null;
		pieces = new HashPiece[s.length];
		int index = 0;
		String thisGroup = "";
		for (int i = 0; i < s.length; i++) {
			if (s[i] == ' ') {
				lastGroup = pg;
				pg = new HashGroup(thisGroup.toCharArray(), lastGroup, pieces,
						index);
				index = pg.empty + 1;
				hash = hash.add(pg.groupHash);
				g.add(pg);
				thisGroup = "";
			} else {
				if(os>0){
					thisGroup += 'O';
					os--;
				}else{
					thisGroup += 'X';
					xs--;
				}
			}
		}
		if(xs>0)
			throw new Exception("os="+numOs+", and xs="+(numPieces-numOs)+", but frame=\n"+String.copyValueOf(s));
		lastGroup = pg;
		pg = new HashGroup(thisGroup.toCharArray(), lastGroup, pieces,
				index);
		index = pg.empty + 1;
		hash = hash.add(pg.groupHash);
		g.add(pg);
		thisGroup = "";
		arrangements = pg.lastPiece.nextX;
		groups = g.toArray(new HashGroup[g.size()]);
	}

	/**
	 * @param s A character representation of the board (in 'X' 'O' and ' ')
	 * @throws Exception If the char array contains other characters
	 */
	public Rearranger(String s) throws Exception {
		this(s.toCharArray());
	}

	/**
	 * 
	 * @param s A character outline telling where the pieces and spaces are (' ' is space, anything else is piece)
	 * @param os The number of O's on the board
	 * @param xs The number of X's on the board
	 * @throws Exception If the number of pieces in the outline is not os + xs
	 */
	public Rearranger(String s, int os, int xs) throws Exception {
		this(s.toCharArray(),os,xs);
	}

	/**
	 * @param player 'X' or 'O', the piece to be added to the board
	 * @return A collection of all the hashes after each possible move is made.
	 */
	public Collection<Pair<Integer, BigInteger>> getChildren(char player) {
		ArrayList<Pair<Integer, BigInteger>> result = new ArrayList<Pair<Integer, BigInteger>>(
				groups.length);
		BigInteger move = hash;
		if (player == 'O')
			for (int i = groups.length - 2; i >= 0; i--) {
				result.add(new Pair<Integer, BigInteger>(groups[i].empty, move
						.add(groups[i].lastPiece.nextO)));
				move = move.add(groups[i].addO);
			}
		else if (player == 'X')
			for (int i = groups.length - 2; i >= 0; i--) {
				result.add(new Pair<Integer, BigInteger>(groups[i].empty, move));
				move = move.add(groups[i].addX);
			}
		return result;
	}

	/**
	 * @return Whether these characters have another arrangement.
	 */
	public boolean hasNext() {
		return hasNext;
	}

	/**
	 * Each time next() is called, the pieces assume their new positions in the
	 * next hash and a list of all the pieces that were changed is returned.
	 * It's expected that the calling program will use this list to speed up
	 * win-checking (if possible).
	 * 
	 * @return The indices of all the pieces that were changed paired with the
	 *         characters they were changed to
	 */
	public Collection<Pair<Integer, Character>> next() {
		ArrayList<Pair<Integer, Character>> changedPieces = new ArrayList<Pair<Integer, Character>>(
				2 * Math.min(openO - 1, openX) + 2);
		int piece = -1;
		piece = nextPiece(piece);
		int i;
		HashPiece lastPiece = null;
		if (openX > 0 && openO > 1) {
			for (i = 0; i < openO - 1; i++) {
				if (get(piece) == 'X')
					changedPieces.add(new Pair<Integer, Character>(piece, 'O'));
				pieces[piece].set(i + 1, BigInteger.ZERO, 'O');
				lastPiece = pieces[piece];
				piece = nextPiece(piece);
			}
			for (i = 0; i < openX; i++) {
				if (get(piece) == 'O')
					changedPieces.add(new Pair<Integer, Character>(piece, 'X'));
				pieces[piece].set(openO - 1, lastPiece.nextX, 'X');
				lastPiece = pieces[piece];
				piece = nextPiece(piece);
			}
		} else if (openX == 0 && openO == 1) {
			lastPiece = null;
		} else {
			for (i = 0; i < openX + openO - 2; i++)
				piece = nextPiece(piece);
			lastPiece = pieces[piece];
			piece = nextPiece(piece);
		}
		changedPieces.add(new Pair<Integer,Character>(piece, 'X'));
		if (lastPiece == null)
			pieces[piece].set(openO - 1, BigInteger.ONE, 'X');
		else
			pieces[piece].set(openO - 1, lastPiece.nextX, 'X');
		lastPiece = pieces[piece];
		piece = nextPiece(piece);
		changedPieces.add(new Pair<Integer,Character>(piece, 'O'));
		pieces[piece].set(openO, lastPiece.nextO, 'O');
		if (openO > 1) {
			openX = 0;
			openO--;
		} else {
			openX++;
			piece = nextPiece(piece);
			while (get(piece) == 'O') {
				openO++;
				piece = nextPiece(piece);
				if (piece == -1) {
					hasNext = false;
					break;
				}
			}
		}
		hash = hash.add(BigInteger.ONE);
		return changedPieces;
	}

	private int prevPiece(int piece) {
		piece--;
		if (piece < 0)
			return -1;
		while (pieces[piece] == null) {
			piece--;
			if (piece < 0)
				return -1;
		}
		return piece;
	}
	
	private int nextPiece(int piece) {
		piece++;
		if (piece >= pieces.length)
			return -1;
		while (pieces[piece] == null) {
			piece++;
			if (piece >= pieces.length)
				return -1;
		}
		return piece;
	}

	/**
	 * @param piece The index of the piece to return
	 * @return The character of the piece.
	 */
	public char get(int piece) {
		if(pieces[piece]==null)
			return ' ';
		else
			return pieces[piece].player;
	}

	@Override
	public String toString() {
		char[] p = new char[pieces.length];
		for (int i = 0; i < pieces.length; i++) {
			if (pieces[i] == null)
				p[i] = ' ';
			else
				p[i] = pieces[i].player;
		}
		return String.valueOf(p);
	}
	
	@Override
	public Rearranger clone(){
		try {
			return new Rearranger(toString().toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return The current hash value
	 */
	public BigInteger getHash() {
		return hash;
	}
	
	/**
	 * Sets the board to the position represented by the given hash
	 * @param hash The hash
	 * @return The new layout of the board
	 */
	public String unHash(BigInteger hash) {
		this.hash = hash;
		int index = prevPiece(pieces.length);
		openO = 0;
		openX = 0;
		BigInteger tryHash = arrangements.multiply(BigInteger.valueOf(numPieces
				- numOs));
		int numOs = this.numOs;
		for (int numPieces = this.numPieces; numPieces > 0; numPieces--) {
			tryHash = tryHash.divide(BigInteger.valueOf(numPieces));
			if (hash.compareTo(tryHash) >= 0) {
				hash = hash.subtract(tryHash);
				pieces[index].set(numOs, tryHash, 'O');
				tryHash = tryHash.multiply(BigInteger.valueOf(numOs));
				numOs--;
				if(openX>0){
					openO = 1;
					openX = 0;
				}else
					openO++;
			} else {
				pieces[index].set(numOs, tryHash, 'X');
				tryHash = tryHash.multiply(BigInteger
						.valueOf(numPieces - numOs - 1));
				openX++;
			}
			index = prevPiece(index);
		}
		return toString();
	}
	public static void main(String[] args){
		try {
			Rearranger iah = new Rearranger("TT TTT TTTT",4,5);
			System.out.println(iah);
			while (iah.hasNext()) {
				iah.next();
				System.out.println(iah);
			}
			iah.unHash(BigInteger.valueOf(32));
			System.out.println(iah);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
