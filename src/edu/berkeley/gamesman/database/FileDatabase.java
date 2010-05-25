package edu.berkeley.gamesman.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import edu.berkeley.gamesman.core.Configuration;

public final class FileDatabase extends Database {

	/**
	 * The file contained in this FileDatabase
	 */
	public final File myFile;

	protected final RandomAccessFile fd;

	protected final long offset;

	public FileDatabase(String uri, Configuration config, boolean solve,
			long firstRecord, long numRecords) throws IOException {
		this(uri, config, solve, firstRecord, numRecords, true);
	}

	public FileDatabase(String uri, Configuration config, boolean solve,
			long firstRecord, long numRecords, boolean storeConf)
			throws IOException {
		super(uri, config, solve, firstRecord, numRecords);
		myFile = new File(uri);
		if (solve) {
			FileOutputStream fos = new FileOutputStream(myFile);
			if (storeConf) {
				store(fos);
			} else
				storeNone(fos);
			offset = fos.getChannel().position() - firstContainedRecord;
			fos.close();
			fd = new RandomAccessFile(myFile, "rw");
			fd.setLength(offset + numContainedRecords);
		} else {
			FileInputStream fis = new FileInputStream(myFile);
			skipHeader(fis);
			offset = fis.getChannel().position() - firstContainedRecord;
			fis.close();
			fd = new RandomAccessFile(myFile, "r");
		}
	}

	@Override
	protected void closeDatabase() {
		try {
			fd.close();
		} catch (IOException e) {
			new Error("Error while closing input stream for database: ", e)
					.printStackTrace();
		}
	}

	@Override
	protected synchronized void seek(long loc) {
		try {
			fd.seek(loc + offset);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	protected synchronized void getBytes(byte[] arr, int off, int len) {
		try {
			fd.read(arr, off, len);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	protected synchronized void putBytes(byte[] arr, int off, int len) {
		try {
			fd.write(arr, off, len);
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	@Override
	protected synchronized void getBytes(DatabaseHandle dh, long loc,
			byte[] arr, int off, int len) {
		seek(loc);
		getBytes(arr, off, len);
	}

	@Override
	protected synchronized void putBytes(DatabaseHandle dh, long loc,
			byte[] arr, int off, int len) {
		seek(loc);
		putBytes(arr, off, len);
	}
}
