package edu.berkeley.gamesman.database;

import edu.berkeley.gamesman.core.Database;
import edu.berkeley.gamesman.core.RecordGroup;
import edu.berkeley.gamesman.util.DebugFacility;
import edu.berkeley.gamesman.util.Util;

/**
 * Test DataBase for GamesCrafters Java. Right now it just writes BigIntegers to
 * memory, without byte padding.
 * 
 * 
 * @author Alex Trofimov
 * @version 1.4
 * 
 *          Change log: 05/05/09 - 1.4 - putByte() is now synchronized, for
 *          multi-threading. This is really important. 03/20/09 - 1.3 - With
 *          data sizes < 58 bits, longs are used instead of BigInts, 20%
 *          speedup. 03/15/09 - 1.2 - Slight speedup for operating on small data
 *          (< 8 bits); ensureCapacity() added. 02/22/09 - 1.1 - Switched to a
 *          byte[] instead of ArrayList<Byte> for internal storage. 02/21/09 -
 *          1.0 - Initial (working) Version.
 */
public class MemoryDatabase extends Database {

	/* Class Variables */
	private byte[] memoryStorage; // byte array to store the data

	protected byte[] rawRecord;

	protected boolean open; // whether this database is initialized

	// and not closed.

	/**
	 * Null Constructor, used primarily for testing. It doesn't set anything
	 * 
	 * @author Alex Trofimov
	 */
	public MemoryDatabase() {
	}

	@Override
	public RecordGroup getRecordGroup(long loc) {
		for (int i = 0; i < conf.recordGroupByteLength; i++) {
			rawRecord[i] = getByte(loc++);
		}
		return new RecordGroup(conf, rawRecord);
	}

	@Override
	public void initialize(String locations) {
		this.initialize();
	}

	/**
	 * Null Constructor for testing the database outside of Gamesman
	 * environment. Initializes the internal storage.
	 * 
	 * @author Alex Trofimov
	 */
	private void initialize() {
		System.out.println(getByteSize());
		this.memoryStorage = new byte[(int) getByteSize()];
		this.open = true;
		rawRecord = new byte[conf.recordGroupByteLength];
	}

	@Override
	public void putRecordGroup(long loc, RecordGroup value) {
		byte[] putRecord = value.getState().toByteArray();
		if (putRecord.length > conf.recordGroupByteLength) {
			int top = conf.recordGroupByteLength + 1;
			for (int i = 1; i < top; i++)
				putByte(loc++, putRecord[i]);
		} else {
			int i;
			for (i = putRecord.length; i < conf.recordGroupByteLength; i++)
				putByte(loc++, (byte) 0);
			for (i = 0; i < putRecord.length; i++)
				putByte(loc++, putRecord[i]);
		}
	}

	@Override
	public void flush() {
		assert Util.debug(DebugFacility.DATABASE,
				"Flushing Memory DataBase. Does Nothing.");
	}

	@Override
	public void close() {
		this.open = false;
		flush();
		assert Util.debug(DebugFacility.DATABASE,
				"Closing Memory DataBase. Does Nothing.");
	}

	/**
	 * Get a byte from the database.
	 * 
	 * @author Alex Trofimov
	 * @param index sequential number of this byte in DB.
	 * @return - one byte at specified byte index.
	 */
	protected byte getByte(long index) {
		return this.memoryStorage[(int) index];
	}

	/**
	 * Write a byte into the database. Assumes that space is already allocated.
	 * 
	 * @author Alex Trofimov
	 * @param index sequential number of byte in DB.
	 * @param data byte that needs to be written.
	 */
	synchronized protected void putByte(long index, byte data) {
		this.memoryStorage[(int) index] = data;
	}
}
