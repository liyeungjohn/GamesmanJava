package edu.berkeley.gamesman.database;

public final class DatabaseLogic {
	private final int shamt;
	public final int recordBytes;

	public DatabaseLogic(long recordStates) {
		int shamt = -1;
		do {
			shamt++;
			recordStates >>= (8 << shamt);
		} while (recordStates > 0);
		this.shamt = shamt;
		recordBytes = 1 << shamt;
	}

	public long getByteIndex(long recordIndex) {
		assert recordIndex >= 0;
		return getNumBytes(recordIndex);
	}

	public long getRecord(byte[] bytes, int off) {
		long record = 0L;
		off += recordBytes - 1;
		for (int i = recordBytes - 1; i >= 0; i--) {
			record <<= 8;
			record |= bytes[off--];
		}
		return record;
	}

	public void fillBytes(long record, byte[] bytes, int off) {
		for (int i = 0; i < recordBytes; i++) {
			bytes[off++] = (byte) record;
			record >>= 8;
		}
	}

	public long getNumBytes(long numRecords) {
		return numRecords << shamt;
	}

	public long getNumRecords(long numBytes) {
		return numBytes >> shamt;
	}
}
