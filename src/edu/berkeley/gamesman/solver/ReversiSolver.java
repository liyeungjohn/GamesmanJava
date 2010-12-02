package edu.berkeley.gamesman.solver;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import edu.berkeley.gamesman.core.*;
import edu.berkeley.gamesman.database.Database;
import edu.berkeley.gamesman.database.DatabaseHandle;
import edu.berkeley.gamesman.game.Reversi;
import edu.berkeley.gamesman.game.util.TierState;
import edu.berkeley.gamesman.util.*;

/**
 * @author DNSpies
 */
public class ReversiSolver extends Solver {

	/**
	 * The default constructor
	 * 
	 * @param conf
	 *            The configuration object
	 */
	public ReversiSolver(Configuration conf) {
		super(conf);
		maxMem = conf.getLong("gamesman.memory", 1 << 25);
		numThreads = conf.getInteger("gamesman.threads", 1);
		minSplits = conf.getInteger("gamesman.split", numThreads);
		minSplitSize = conf.getInteger("gamesman.minimum.split", 1);
	}

	protected int splits;

	private int count;

	protected final int numThreads;

	protected final int minSplits;

	protected final long maxMem;

	private long[] starts;

	private File minSolvedFile = null;

	boolean parallelSolving;

	protected long times[] = new long[7];

	private final int minSplitSize;

	boolean finalRun = true;

	protected void solvePartialTier(Configuration conf, long start,
			long hashes, ReversiSolverUpdater t, Database readDb,
			DatabaseHandle readDh, Database writeDb, DatabaseHandle writeDh) {
		final long firstNano;
		long nano = 0;
		final boolean debugSolver = Util.debug(DebugFacility.SOLVER);
		if (debugSolver) {
			for (int i = 0; i < 7; i++) {
				times[i] = 0;
			}
			firstNano = System.nanoTime();
			nano = firstNano;
		} else
			firstNano = 0;
		Reversi game = (Reversi) conf.getGame();
		long current = start;
		long stepNum = current % STEP_SIZE;
		TierState curState = game.hashToState(start);
		game.setState(curState);
		Record record = game.newRecord();
		Record bestRecord = game.newRecord();
		TierState[] children = new TierState[game.maxChildren()];
		for (int i = 0; i < children.length; i++)
			children[i] = game.newState();
		long lastNano;
		if (debugSolver) {
			lastNano = nano;
			nano = System.nanoTime();
			times[0] = nano - lastNano;
		}
		for (long count = 0L; count < hashes; count++) {
			if (stepNum == STEP_SIZE) {
				t.calculated(STEP_SIZE);
				stepNum = 0;
			}
			Value pv = game.primitiveValue();
			if (debugSolver) {
				lastNano = nano;
				nano = System.nanoTime();
				times[1] += nano - lastNano;
			}
			if (pv == Value.UNDECIDED) {
				int len = game.validMoves(children);
				if (debugSolver) {
					lastNano = nano;
					nano = System.nanoTime();
					times[2] += nano - lastNano;
				}
				bestRecord.value = Value.UNDECIDED;
				for (int i = 0; i < len; i++) {
					game.longToRecord(
							children[i],
							readDb.getRecord(readDh,
									game.stateToHash(children[i])), record);
					record.previousPosition();
					if (bestRecord.value == Value.UNDECIDED
							|| record.compareTo(bestRecord) > 0)
						bestRecord.set(record);
				}
				if (debugSolver) {
					lastNano = nano;
					nano = System.nanoTime();
					times[3] += nano - lastNano;
				}
				writeDb.putRecord(writeDh, current,
						game.recordToLong(curState, bestRecord));
			} else if (pv != Value.IMPOSSIBLE) {
				if (debugSolver) {
					lastNano = nano;
					nano = System.nanoTime();
					times[2] += nano - lastNano;
					lastNano = nano;
					nano = System.nanoTime();
					times[3] += nano - lastNano;
				}
				record.remoteness = 0;
				record.value = pv;
				writeDb.putRecord(writeDh, current,
						game.recordToLong(curState, record));
			}
			if (debugSolver) {
				lastNano = nano;
				nano = System.nanoTime();
				times[4] += nano - lastNano;
			}
			if (count < hashes - 1) {
				game.nextHashInTier();
				curState.hash++;
			}
			++current;
			++stepNum;
			if (debugSolver) {
				lastNano = nano;
				nano = System.nanoTime();
				times[5] += nano - lastNano;
				lastNano = nano;
				nano = System.nanoTime();
				times[6] += nano - lastNano;
			}
		}
		if (debugSolver) {
			long sumTimes = nano - firstNano - times[6] * 6;
			Util.debug(DebugFacility.SOLVER, "Initializing: " + 1000 * times[0]
					/ sumTimes / 10D);
			Util.debug(DebugFacility.SOLVER, "Primitive Value: " + 1000
					* (times[1] - times[6]) / sumTimes / 10D);
			Util.debug(DebugFacility.SOLVER, "Calculating Chilren: " + 1000
					* (times[2] - times[6]) / sumTimes / 10D);
			Util.debug(DebugFacility.SOLVER, "Reading Children: " + 1000
					* (times[3] - times[6]) / sumTimes / 10D);
			Util.debug(DebugFacility.SOLVER, "Storing records: " + 1000
					* (times[4] - times[6]) / sumTimes / 10D);
			Util.debug(DebugFacility.SOLVER, "Stepping: " + 1000
					* (times[5] - times[6]) / sumTimes / 10D);
		}
	}

	@Override
	public WorkUnit prepareSolve(Configuration inconf) {
		String msf = inconf.getProperty("gamesman.minSolvedFile", null);
		if (msf == null)
			tier = ((Reversi) inconf.getGame()).numberOfTiers();
		else {
			minSolvedFile = new File(msf);
			if (minSolvedFile.exists()) {
				try {
					Scanner scan = new Scanner(minSolvedFile);
					tier = scan.nextInt();
					scan.close();
				} catch (FileNotFoundException e) {
					throw new Error("This should never happen", e);
				}
			} else {
				tier = ((Reversi) inconf.getGame()).numberOfTiers();
				try {
					minSolvedFile.createNewFile();
					FileWriter fw = new FileWriter(minSolvedFile);
					fw.write(Integer.toString(tier));
					fw.close();
				} catch (IOException e) {
					throw new Error(e);
				}
			}
		}
		updater = new ReversiSolverUpdater();
		parallelSolving = false;
		flusher.run();
		needs2Reset = false;
		return new ReversiSolverWorkUnit(inconf);
	}

	protected int nextIndex = 0;

	protected ReversiSolverUpdater updater;

	protected CyclicBarrier barr;

	private final Runnable flusher = new Runnable() {
		public void run() {
			if (minSolvedFile != null) {
				try {
					FileWriter fw;
					fw = new FileWriter(minSolvedFile);
					fw.write(Integer.toString(tier));
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (finalRun)
				--tier;
			needs2Sync = false;
			if (tier < 0) {
				updater.complete();
				if (minSolvedFile != null)
					minSolvedFile.delete();
			} else {
				if (barr != null)
					needs2Reset = true;
				Reversi game = (Reversi) conf.getGame();
				long fullStart = game.hashOffsetForTier(tier);
				long fullSize = game.numHashesForTier(tier);
				splits = Math.max(minSplits, numSplits(tier, maxMem));
				if (finalRun) {
					finalRun = false;
				} else {
					finalRun = true;
					fullSize /= 2;
				}
				starts = writeDb.splitRange(fullStart, fullSize, splits,
						minSplitSize);
			}
		}
	};

	protected int tier;

	private volatile boolean needs2Sync = false;

	private volatile boolean needs2Reset = false;

	protected int numSplits(int tier, long maxMem) {
		return numSplits(tier, maxMem,
				((Reversi) conf.getGame()).numHashesForTier(tier));
	}

	protected int numSplits(int tier, long maxMem, long numHashes) {
		return 1;
	}

	protected Pair<Long, Long> nextSlice(Configuration conf) {
		while (true) {
			if (needs2Sync) {
				if (parallelSolving) {
					updater.complete();
					return null;
				}
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
						throw new Error(
								"ReversiSolver thread was interrupted while waiting!",
								e);
					} catch (BrokenBarrierException e) {
						throw new Error("Barrier Broken", e);
					}
				}
			}
			synchronized (this) {
				if (!needs2Sync) {
					if (tier < 0) {
						return null;
					}
					Pair<Long, Long> slice = new Pair<Long, Long>(
							starts[count], starts[count + 1] - starts[count]);
					assert Util.debug(DebugFacility.THREADING, "Solving "
							+ (count + 1) + "/" + (starts.length - 1)
							+ " in tier " + tier + "; " + starts[count] + "-"
							+ starts[count + 1]);
					if (count < starts.length - 2) {
						++count;
					} else {
						count = 0;
						needs2Sync = true;
					}
					return slice;
				}
			}
		}
	}

	protected class ReversiSolverWorkUnit implements WorkUnit {

		protected int index;

		Configuration conf;

		Pair<Long, Long> thisSlice;

		ReversiSolverWorkUnit(Configuration conf) {
			this.conf = conf;
			this.index = nextIndex++;
		}

		public void conquer() {
			assert Util.debug(DebugFacility.SOLVER, "Started the solver... ("
					+ index + ")");
			Thread.currentThread().setName(
					"Solver (" + index + "): " + conf.getGame().describe());
			Pair<Long, Long> slice;
			while ((slice = nextSlice(conf)) != null) {
				thisSlice = slice;
				DatabaseHandle myWrite = writeDb.getHandle();
				DatabaseHandle readHandle;
				if (readDb == null)
					readHandle = null;
				else
					readHandle = readDb.getHandle();
				if (finalRun)
					secondRun(conf, slice.car, slice.cdr, updater, readDb,
							readHandle, writeDb, myWrite);
				else
					solvePartialTier(conf, slice.car, slice.cdr, updater,
							readDb, readHandle, writeDb, myWrite);
				writeDb.closeHandle(myWrite);
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

		public ArrayList<WorkUnit> divide(int num) {
			ArrayList<WorkUnit> arr = new ArrayList<WorkUnit>(num);
			arr.add(this);
			for (int i = 1; i < num; i++)
				arr.add(new ReversiSolverWorkUnit(conf.cloneAll()));
			if (parallelSolving || numThreads == 1)
				barr = null;
			else
				barr = new CyclicBarrier(numThreads, flusher);
			return arr;
		}

		@Override
		public String toString() {
			String str = "WorkUnit " + index + "; slice is ";
			if (thisSlice != null) {
				str += "[" + thisSlice.car + "-" + thisSlice.cdr + "]";
			} else {
				str += "null";
			}
			return str;
		}
	}

	final class ReversiSolverUpdater {

		private long total = 0;

		private Task t;

		ReversiSolverUpdater() {
			this(conf.getGame().numHashes());
		}

		ReversiSolverUpdater(long totalProgress) {
			Reversi myGame = (Reversi) conf.getGame();
			t = Task.beginTask("Tier solving \"" + myGame.describe() + "\"");
			t.setTotal(totalProgress);
		}

		synchronized void calculated(int howMuch) {
			total += howMuch;
			if (t != null) {
				t.setProgress(total);
			}
		}

		public void complete() {
			if (t != null)
				t.complete();
			t = null;
		}
	}

	/**
	 * @param conf
	 *            The configuration object
	 * @param tier
	 *            The tier
	 * @param startHash
	 *            The first hash in the range to solve
	 * @param numHashes
	 *            The number of hashes to solve
	 * @return A WorkUnit for solving solveSpace
	 */
	public WorkUnit prepareSolve(Configuration conf, int tier, long startHash,
			long numHashes) {
		this.tier = tier;
		updater = new ReversiSolverUpdater(numHashes);
		parallelSolving = true;
		splits = Math.max(minSplits, numSplits(tier, maxMem, numHashes));
		starts = writeDb.splitRange(startHash, numHashes, splits, minSplitSize);
		return new ReversiSolverWorkUnit(conf);
	}

	public void secondRun(Configuration conf, long start, long hashes,
			ReversiSolverUpdater t, Database readDb, DatabaseHandle readDh,
			Database writeDb, DatabaseHandle writeDh) {
		Reversi game = (Reversi) conf.getGame();
		long otherSide = game.numHashesForTier(tier) / 2;
		Record r1 = game.newRecord(), r2 = game.newRecord();
		for (long pos = start; pos < start + hashes; pos++) {
			game.longToRecord(null, readDb.getRecord(readDh, pos), r1);
			game.longToRecord(null, readDb.getRecord(readDh, pos + otherSide),
					r2);
			if (r1.remoteness == 0 && r2.remoteness != 0) {
				r1.set(r2);
				r1.previousPosition();
				writeDb.putRecord(writeDh, pos, game.recordToLong(null, r1));
			}
			if (r2.remoteness == 0 && r1.remoteness != 0) {
				r2.set(r1);
				r2.previousPosition();
				writeDb.putRecord(writeDh, pos + otherSide,
						game.recordToLong(null, r2));
			}
		}
	}

}