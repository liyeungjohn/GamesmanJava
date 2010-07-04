package edu.berkeley.gamesman.database;

public final class DatabaseHeader {
	public final long firstRecord;

	public final long numRecords;

	final int recordsPerGroup;

	final int recordGroupByteLength;

	final int recordGroupByteBits;

	final boolean superCompress;

	DatabaseHeader(long firstRecord, long numRecords, int recordsPerGroup,
			int recordGroupByteLength) {
		superCompress = true;
		recordGroupByteBits = -1;
		this.firstRecord = firstRecord;
		this.numRecords = numRecords;
		this.recordsPerGroup = recordsPerGroup;
		this.recordGroupByteLength = recordGroupByteLength;
	}

	DatabaseHeader(long firstRecord, long numRecords, int recordGroupByteBits) {
		superCompress = false;
		this.recordGroupByteBits = recordGroupByteBits;
		this.firstRecord = firstRecord;
		this.numRecords = numRecords;
		this.recordsPerGroup = 1;
		this.recordGroupByteLength = 1 << recordGroupByteBits;
	}

	public DatabaseHeader(byte[] dbInfo) {
		long firstContainedRecord = 0;
		for (int i = 0; i < 8; i++) {
			firstContainedRecord <<= 8;
			firstContainedRecord |= (dbInfo[i] & 255);
		}
		this.firstRecord = firstContainedRecord;
		long numContainedRecords = 0;
		for (int i = 8; i < 16; i++) {
			numContainedRecords <<= 8;
			numContainedRecords |= (dbInfo[i] & 255);
		}
		this.numRecords = numContainedRecords;
		int firstByte = dbInfo[16];
		int secondByte = 255 & dbInfo[17];
		if (firstByte < 0) {
			superCompress = false;
			recordsPerGroup = 1;
			recordGroupByteBits = secondByte;
			recordGroupByteLength = 1 << secondByte;
		} else {
			superCompress = true;
			recordsPerGroup = (firstByte << 2) | (secondByte >> 6);
			recordGroupByteLength = secondByte & 63;
			recordGroupByteBits = -1;
		}
	}

	public byte[] toBytes() {
		int c = 0;
		byte[] b = new byte[18];
		for (int i = 56; i >= 0; i -= 8)
			b[c++] = (byte) (firstRecord >>> i);
		for (int i = 56; i >= 0; i -= 8)
			b[c++] = (byte) (numRecords >>> i);
		if (superCompress) {
			b[c++] = (byte) (recordsPerGroup >>> 2);
			b[c++] = (byte) (((recordsPerGroup & 3) << 6) | recordGroupByteLength);
		} else {
			b[c++] = -1;
			b[c++] = (byte) recordGroupByteBits;
		}
		return b;
	}

	public final DatabaseHeader getHeader(long firstRecord, long numRecords) {
		if (superCompress)
			return new DatabaseHeader(firstRecord, numRecords, recordsPerGroup,
					recordGroupByteLength);
		else
			return new DatabaseHeader(firstRecord, numRecords,
					recordGroupByteBits);
	}
}