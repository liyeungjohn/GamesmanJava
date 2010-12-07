package edu.berkeley.gamesman;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.State;
import edu.berkeley.gamesman.game.Game;
import edu.berkeley.gamesman.master.Master;
import edu.berkeley.gamesman.solver.Solver;
import edu.berkeley.gamesman.util.DebugFacility;
import edu.berkeley.gamesman.util.Pair;
import edu.berkeley.gamesman.util.Util;

/**
 * @author Steven Schlansker
 */
public final class GamesmanMain extends GamesmanApplication {
	private Configuration conf;

	/**
	 * No arg constructor
	 */
	public GamesmanMain() {
	}

	@Override
	public int run(Properties props) {
		try {
			this.conf = new Configuration(props);
		} catch (ClassNotFoundException e) {
			throw new Error("Configuration contains unknown game", e);
		}
		Thread.currentThread().setName("Gamesman");

		assert Util.debug(DebugFacility.CORE, "Preloading classes...");

		String solverName;
		solverName = conf.getProperty("gamesman.solver");
		if (solverName == null)
			throw new Error(
					"You must specify a solver with the property gamesman.solver");

		Class<? extends Solver> s = null;
		try {
			s = Util.typedForName("edu.berkeley.gamesman.solver." + solverName,
					Solver.class);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String cmd = conf.getProperty("gamesman.command", null);
		if (cmd != null) {
			try {
				this.getClass().getMethod("execute" + cmd, (Class<?>[]) null)
						.invoke(this);
			} catch (NoSuchMethodException nsme) {
				System.out.println("Don't know how to execute command " + nsme);
			} catch (IllegalAccessException iae) {
				System.out.println("Permission denied while executing command "
						+ iae);
			} catch (InvocationTargetException ite) {
				System.out.println("Exception while executing command: " + ite);
				ite.getTargetException().printStackTrace();
			}
		} else {
			assert Util.debug(DebugFacility.CORE, "Defaulting to solve...");
			Master m = new Master(conf, s);
			m.run();
		}

		assert Util.debug(DebugFacility.CORE, "Finished run, tearing down...");
		return 0;
	}

	/**
	 * Diagnostic call to unhash an arbitrary value to a game board
	 * @param <T> The state type of the game
	 */
	public <T extends State> void executeunhash() {
		Game<T> gm = conf.getCheckedGame();
		T state = gm.hashToState(Long.parseLong(conf
				.getProperty("gamesman.hash")));
		System.out.println(gm.displayState(state));
	}

	/**
	 * Diagnostic call to view all child moves of a given hashed game state
	 * @param <T> The state type of the game
	 */
	public <T extends State> void executegenmoves() {
		Game<T> gm = conf.getCheckedGame();
		T state = gm.hashToState(Long.parseLong(conf
				.getProperty("gamesman.hash")));
		for (Pair<String, T> nextstate : gm.validMoves(state)) {
			System.out.println(gm.stateToHash(nextstate.cdr));
			System.out.println(gm.displayState(nextstate.cdr));
		}
	}

	/**
	 * Hash a single board with the given hasher and print it.
	 * @param <T> The state type of the game
	 */
	public <T extends State> void executehash() {
		Game<T> gm = conf.getCheckedGame();
		String str = conf.getProperty("board");
		if (str == null)
			throw new Error("Please specify a board to hash");
		System.out.println(gm.stateToHash(gm.stringToState(str.toUpperCase())));
	}

	/**
	 * Evaluate a single board and return its primitive value.
	 * @param <T> The state type of the game
	 */
	public <T extends State> void executeevaluate() {
		Game<T> gm = conf.getCheckedGame();
		String board = conf.getProperty("gamesman.board");
		if (board == null)
			throw new Error("Please specify a hash to evaluate");
		long val = Long.parseLong(board);
		System.out.println(gm.strictPrimitiveValue(gm.hashToState(val)));
	}

	// public void executetestRPC(){
	// new RPCTest();
	// }

}
