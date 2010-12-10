package edu.berkeley.gamesman.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import edu.berkeley.gamesman.core.Configuration;

/**
 * A database wrapper for multiple underlying databases which may or may not be
 * of the same type
 * 
 * @author dnspies
 */
public class SplitFileSystemDatabase extends Database {
	private final GZippedFileSystemDatabase[] databaseList;
	private final long[] firstByteIndices;
	private final int[] firstNums;
	private final long[] lastByteIndices;
	private final int[] lastNums;
	private long size;

	private static class Info {
		public Info(FileSystem fs, String uri, long firstRecord, long numRecords)
				throws IOException {
			this.fs = fs;
			this.uri = uri;
			this.solve = false;
			this.firstRecord = firstRecord;
			this.numRecords = numRecords;
			FSDataInputStream in = fs.open(new Path(uri));
			byte[] headBytes = new byte[18];
			in.readFully(headBytes);
			this.header = new DatabaseHeader(headBytes);
			try {
				conf = Configuration.load(in);
			} catch (ClassNotFoundException e) {
				throw new Error(e);
			}
			dbStream = new Scanner(in);
		}

		private final String uri;
		private final Configuration conf;
		private final boolean solve;
		private final long firstRecord;
		private final long numRecords;
		private final DatabaseHeader header;
		private final Scanner dbStream;
		private final FileSystem fs;
	}

	public SplitFileSystemDatabase(FileSystem fs, String uri, long firstRecord,
			long numRecords) throws IOException {
		this(new Info(fs, uri, firstRecord, numRecords));
	}

	private SplitFileSystemDatabase(Info info) throws IOException {
		super(info.uri, info.conf, info.solve, info.firstRecord,
				info.numRecords, info.header);
		Scanner dbStream = info.dbStream;
		ArrayList<GZippedFileSystemDatabase> databaseList = new ArrayList<GZippedFileSystemDatabase>();
		String dbType = dbStream.next();
		while (!dbType.equals("end")) {
			String dbUri = dbStream.next();
			long dbFirstRecord = dbStream.nextLong();
			long dbNumRecords = dbStream.nextLong();
			databaseList.add(new GZippedFileSystemDatabase(info.fs, dbUri,
					dbFirstRecord, dbNumRecords));
			dbType = dbStream.next();
		}
		dbStream.close();
		this.databaseList = databaseList
				.toArray(new GZippedFileSystemDatabase[databaseList.size()]);
		firstByteIndices = new long[databaseList.size()];
		firstNums = new int[databaseList.size()];
		lastByteIndices = new long[databaseList.size()];
		lastNums = new int[databaseList.size()];
		for (int i = 0; i < lastByteIndices.length; i++) {
			GZippedFileSystemDatabase db = this.databaseList[i];
			long dbFirstRecord = db.firstRecord();
			firstByteIndices[i] = toByte(dbFirstRecord);
			firstNums[i] = toNum(dbFirstRecord);
			long dbLastRecord = dbFirstRecord + db.numRecords();
			lastByteIndices[i] = lastByte(dbLastRecord);
			lastNums[i] = toNum(dbLastRecord);
		}
	}

	@Override
	public void close() {
		for (GZippedFileSystemDatabase d : databaseList)
			d.close();
	}

	@Override
	protected void getBytes(DatabaseHandle dh, long loc, byte[] arr, int off,
			int len) {
		getRecordsAsBytes(dh, loc, 0, arr, off, len, 0, true);
	}

	@Override
	protected void prepareRange(DatabaseHandle dh, long byteIndex,
			int firstNum, long numBytes, int lastNum) {
		super.prepareRange(dh, byteIndex, firstNum, numBytes, lastNum);
		SplitHandle sh = (SplitHandle) dh;
		sh.currentDb = binSearch(byteIndex, firstNum);
		long firstNumBytes = lastByteIndices[sh.currentDb] - byteIndex;
		int firstLastNum;
		if (numBytes < firstNumBytes) {
			firstNumBytes = numBytes;
			firstLastNum = lastNum;
		} else if (numBytes > firstNumBytes) {
			firstLastNum = lastNums[sh.currentDb];
		} else {
			firstLastNum = Math.min(lastNum, lastNums[sh.currentDb]);
		}
		databaseList[sh.currentDb].prepareRange(sh.handles[sh.currentDb],
				byteIndex, firstNum, firstNumBytes, firstLastNum);
		sh.dbLoc[sh.currentDb] = byteIndex;
	}

	@Override
	protected int getBytes(DatabaseHandle dh, byte[] arr, int off, int maxLen,
			boolean overwriteEdgesOk) {
		if (!overwriteEdgesOk) {
			return super.getBytes(dh, arr, off, maxLen, false);
		}
		final int numBytes = (int) Math.min(dh.lastByteIndex - dh.location,
				maxLen);
		SplitHandle sh = (SplitHandle) dh;
		while (sh.location >= lastByteIndices[sh.currentDb])
			sh.dbLoc[sh.currentDb++] = -1;
		int db = sh.currentDb;
		while (db < firstByteIndices.length
				&& firstByteIndices[db] < sh.location + numBytes) {
			if (sh.dbLoc[db] < 0) {
				long lastByteIndex = lastByteIndices[db];
				int lastNum;
				if (lastByteIndex > sh.lastByteIndex) {
					lastByteIndex = sh.lastByteIndex;
					lastNum = sh.lastNum;
				} else if (lastByteIndex < sh.lastByteIndex) {
					lastNum = lastNums[db];
				} else
					lastNum = Math.min(sh.lastNum, lastNums[db]);
				if (firstByteIndices[db] < lastByteIndex
						- recordGroupByteLength
						|| lastNum == 0 || firstNums[db] < lastNum) {
					databaseList[db].prepareRange(sh.handles[db],
							firstByteIndices[db], firstNums[db], lastByteIndex
									- firstByteIndices[db], lastNum);
					sh.dbLoc[db] = firstByteIndices[db];
					if (sh.dbLoc[db] < sh.location) {
						byte[] skipBytes = dh.getRecordGroupBytes();
						sh.dbLoc[db] += databaseList[db].getBytes(
								sh.handles[db], skipBytes, 0,
								(int) (sh.location - sh.dbLoc[db]), true);
						dh.releaseBytes(skipBytes);
					}
				} else
					break;
			}
			sh.dbLoc[db] += databaseList[db].getBytes(sh.handles[db], arr, off
					+ (int) (sh.dbLoc[db] - sh.location), numBytes
					- (int) (sh.dbLoc[db] - sh.location), false);
			db++;
		}
		sh.location += numBytes;
		if (sh.lastByteIndex == sh.location) {
			for (int i = sh.currentDb; i < sh.dbLoc.length && sh.dbLoc[i] >= 0; i++)
				sh.dbLoc[i] = -1;
		}
		return numBytes;
	}

	private int binSearch(long byteIndex, int num) {
		int low = 0, high = databaseList.length;
		while (high - low > 1) {
			int guess = (low + high) >>> 1;
			if (firstByteIndices[guess] > byteIndex
					|| (firstByteIndices[guess] == byteIndex && firstNums[guess] > num))
				high = guess;
			else
				low = guess;
		}
		return low;
	}

	private class SplitHandle extends DatabaseHandle {
		public final DatabaseHandle[] handles;
		public int currentDb;
		public final long[] dbLoc;

		public SplitHandle(int recordGroupByteLength) {
			super(recordGroupByteLength);
			handles = new DatabaseHandle[databaseList.length];
			dbLoc = new long[databaseList.length];
			for (int i = 0; i < handles.length; i++) {
				handles[i] = databaseList[i].getHandle();
				dbLoc[i] = -1;
			}
		}
	}

	@Override
	public DatabaseHandle getHandle() {
		return new SplitHandle(recordGroupByteLength);
	}

	@Override
	public long getSize() {
		int percent = 0;
		Thread[] threads = new Thread[8];
		for (int i = 0; i < databaseList.length; i++) {
			final GZippedFileSystemDatabase d = databaseList[i];
			if (i * 100 / databaseList.length > percent) {
				percent = i * 100 / databaseList.length;
				for (Thread t : threads) {
					if (t != null) {
						try {
							t.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println(percent + "%: " + d.firstRecord()
						+ " records = " + size + " bytes");
			}
			if (threads[i & 7] != null && threads[i & 7].isAlive())
				try {
					threads[i & 7].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			threads[i & 7] = new Thread() {
				public void run() {
					long mySize = d.getSize();
					synchronized (SplitFileSystemDatabase.this) {
						size += mySize;
					}
				}
			};
			threads[i & 7].start();
		}
		for (Thread t : threads)
			if (t != null)
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		return size;
	}

	@Override
	protected void putBytes(DatabaseHandle dh, long loc, byte[] arr, int off,
			int len) {
		throw new UnsupportedOperationException();
	}
}
