package edu.berkeley.gamesman.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import edu.berkeley.gamesman.core.*;
import edu.berkeley.gamesman.util.*;

/**
 * TierSolver documentation stub
 * 
 * @author Steven Schlansker
 * @param <T>
 *            The state type for the game
 */
public class TierSolver<T> extends Solver {

	protected TieredGame<T> myGame;

	/**
	 * The number of positions to go through between each update/reset
	 */
	private int split;

	private int count;

	private long[] starts;

	boolean hadooping;

	@Override
	public WorkUnit prepareSolve(Configuration inconf) {

		myGame = Util.checkedCast(inconf.getGame());
		tier = myGame.numberOfTiers();
		updater = new TierSolverUpdater();
		hadooping = false;
		flusher.run();
		needs2Reset = false;
		return new TierSolverWorkUnit(inconf);
	}

	protected void solvePartialTier(Configuration conf, long start, long end,
			TierSolverUpdater t, Database inRead, Database inWrite) {
		long current = start - 1;
		TieredGame<T> game = Util.checkedCast(conf.getGame());

		while (current < end) {
			current++;

			if (current % STEP_SIZE == 0)
				t.calculated(STEP_SIZE);

			T state = game.hashToState(current);

			PrimitiveValue pv = game.primitiveValue(state);

			if (pv.equals(PrimitiveValue.UNDECIDED)) {
				assert Util.debug(DebugFacility.SOLVER,
						"Primitive value for state " + current
								+ " is undecided");
				Collection<Pair<String, T>> children = game.validMoves(state);
				ArrayList<Record> vals = new ArrayList<Record>(children.size());
				for (Pair<String, T> child : children) {
					vals.add(inRead.getRecord(game.stateToHash(child.cdr)));
				}
				Record[] theVals = new Record[vals.size()];
				Record newVal = game.combine(vals.toArray(theVals), 0,
						theVals.length);
				inWrite.putRecord(current, newVal);
			} else {
				Record prim = game.newRecord();
				prim.set(RecordFields.VALUE, pv.value());
				assert Util.debug(DebugFacility.SOLVER,
						"Primitive value for state " + current + " is " + prim);
				inWrite.putRecord(current, prim);
			}
		}
		assert Util.debug(DebugFacility.THREADING,
				"Reached end of partial tier at " + end);

	}

	protected int nextIndex = 0;

	protected TierSolverUpdater updater;

	protected CyclicBarrier barr;

	private final Runnable flusher = new Runnable() {
		public void run() {
			if (writeDb != null)
				writeDb.flush();
			--tier;
			needs2Sync = false;
			if (tier < 0)
				updater.complete();
			else {
				if (barr != null)
					needs2Reset = true;
				long fullStart = myGame.hashOffsetForTier(tier);
				long fullSize = myGame.lastHashValueForTier(tier) + 1L
						- fullStart;
				starts = Util.groupAlignedTasks(split, fullStart, fullSize,
						conf.recordsPerGroup);
			}
		}
	};

	protected int tier;

	private boolean needs2Sync = false;

	private boolean needs2Reset = false;

	protected Pair<Long, Long> nextSlice(Configuration conf) {
		while (true) {
			if (needs2Sync) {
				if (barr == null)
					flusher.run();
				else {
					assert Util.debug(DebugFacility.THREADING,
							"Thread waiting to tier-sync");
					try {
						barr.await();
						synchronized (this) {
							if (needs2Reset) {
								needs2Reset = false;
								barr.reset();
							}
						}
					} catch (InterruptedException e) {
						Util
								.fatalError(
										"TierSolver thread was interrupted while waiting!",
										e);
					} catch (BrokenBarrierException e) {
						Util.fatalError("Barrier Broken", e);
					}
				}
			}
			synchronized (this) {
				if (barr == null || !needs2Sync) {
					if (tier < 0)
						return null;
					Pair<Long, Long> slice = new Pair<Long, Long>(
							starts[count], starts[count + 1] - 1);
					if (count < starts.length - 2) {
						++count;
					} else {
						count = 0;
						needs2Sync = true;
					}
					assert Util.debug(DebugFacility.THREADING,
							"Beginning to solve slice " + slice + " for count "
									+ (needs2Sync ? (split - 1) : (count - 1))
									+ " in tier " + tier);
					return slice;
				}
			}
		}
	}

	private final class TierSolverWorkUnit implements WorkUnit {

		private int index;

		Configuration conf;

		TierSolverWorkUnit(Configuration conf) {
			this.conf = conf;
			this.index = nextIndex++;
		}

		public void conquer() {
			assert Util.debug(DebugFacility.SOLVER, "Started the solver... ("
					+ index + ")");
			Thread.currentThread().setName(
					"Solver (" + index + "): " + myGame.toString());
			Pair<Long, Long> slice;
			while ((slice = nextSlice(conf)) != null) {
				if (slice.car <= slice.cdr) {
					if (hadooping) {
						Database myWrite = writeDb.beginWrite(tier, slice.car,
								slice.cdr + 1);
						solvePartialTier(conf, slice.car, slice.cdr, updater,
								readDb, myWrite);
						writeDb.endWrite(tier, myWrite, slice.car,
								slice.cdr + 1);
					} else
						solvePartialTier(conf, slice.car, slice.cdr, updater,
								readDb, writeDb);
				}
			}
			if (barr != null)
				try {
					barr.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
		}

		public List<WorkUnit> divide(int num) {
			ArrayList<WorkUnit> arr = new ArrayList<WorkUnit>(num);
			arr.add(this);
			for (int i = 1; i < num; i++)
				arr.add(new TierSolverWorkUnit(conf.cloneAll()));
			if (hadooping || num == 1)
				barr = null;
			else
				barr = new CyclicBarrier(num, flusher);
			return arr;
		}
	}

	final class TierSolverUpdater {

		private long total = 0;

		private Task t;

		TierSolverUpdater() {
			t = Task.beginTask("Tier solving \"" + myGame.describe() + "\"");
			t.setTotal(myGame.lastHashValueForTier(myGame.numberOfTiers() - 1));
		}

		synchronized void calculated(int howMuch) {
			total += howMuch;
			if (t != null) {
				t.setProgress(total);
			}
		}

		public synchronized void complete() {
			if (t != null)
				t.complete();
			t = null;
		}
	}

	@Override
	public void initialize(Configuration conf) {
		super.initialize(conf);
		split = conf.getInteger("gamesman.split", conf.getInteger(
				"gamesman.threads", 1));
	}

	/**
	 * @param conf
	 *            The configuration object
	 * @param tier
	 *            The tier
	 * @param startHash
	 *            The tier to solve
	 * @param endHash
	 *            The range in the given tier to solve
	 * @return A WorkUnit for solving solveSpace
	 */
	public WorkUnit prepareSolve(Configuration conf, int tier, long startHash,
			long endHash) {
		myGame = Util.checkedCast(conf.getGame());
		updater = new TierSolverUpdater();
		this.tier = tier;
		starts = Util.groupAlignedTasks(split, startHash, endHash - startHash,
				conf.recordsPerGroup);
		hadooping = true;
		return new TierSolverWorkUnit(conf);
	}
}
