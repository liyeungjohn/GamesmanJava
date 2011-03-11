package edu.berkeley.gamesman.database;

import java.io.IOException;
import java.util.Random;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.State;
import edu.berkeley.gamesman.database.Database;
import edu.berkeley.gamesman.database.DatabaseHandle;
import edu.berkeley.gamesman.game.Game;

public class DummyDatabase extends Database {
	private final long recordStates;
	private final Random randomer;

	private static class Parameters {
		private final Configuration conf;
		private final long firstRecordIndex, numRecords;
		private final long recordStates;
		private final boolean reading, writing;

		public Parameters(Configuration conf, long firstRecord,
				long numRecords, boolean reading, boolean writing) {
			this(conf, conf.getGame(), firstRecord, numRecords, reading,
					writing);
		}

		public Parameters(Configuration conf, boolean reading, boolean writing) {
			this(conf, conf.getGame(), reading, writing);
		}

		private Parameters(Configuration conf, Game<? extends State> game,
				boolean reading, boolean writing) {
			this(conf, game, 0, game.numHashes(), reading, writing);
		}

		private Parameters(Configuration conf, Game<?> g, long firstRecord,
				long numRecords, boolean reading, boolean writing) {
			this.conf = conf;
			this.firstRecordIndex = firstRecord;
			this.numRecords = numRecords;
			recordStates = g.recordStates();
			this.reading = reading;
			this.writing = writing;
		}
	}

	public DummyDatabase(Configuration conf, boolean reading, boolean writing) {
		this(new Parameters(conf, reading, writing));
	}

	public DummyDatabase(String uri, Configuration conf, long firstRecord,
			long numRecords, boolean reading, boolean writing) {
		this(new Parameters(conf, firstRecord, numRecords, reading, writing));
	}

	private DummyDatabase(Parameters p) {
		super(p.conf, p.firstRecordIndex, p.numRecords, p.reading, p.writing);
		this.recordStates = p.recordStates;
		this.randomer = new Random();
	}

	@Override
	protected int readBytes(DatabaseHandle dh, long location, byte[] array,
			int off, int len) {
		return len;
	}

	@Override
	protected int writeBytes(DatabaseHandle dh, long location, byte[] array,
			int off, int len) {
		return len;
	}

	@Override
	public synchronized long readNextRecord(DatabaseHandle dh)
			throws IOException {
		readFullBytes(dh, dh.currentRecord, 0, myLogic.recordBytes);
		return nextRecord();
	}

	@Override
	protected long readRecordFromByteIndex(DatabaseHandle dh, long byteIndex)
			throws IOException {
		readFullBytes(dh, byteIndex, dh.currentRecord, 0, myLogic.recordBytes);
		return nextRecord();
	}

	private long nextRecord() {
		return (long) (randomer.nextDouble() * recordStates);
	}
}