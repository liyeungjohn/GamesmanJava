package edu.berkeley.gamesman.database;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.Database;
import edu.berkeley.gamesman.util.Util;

/**
 * The FileDatabase is a database designed to write directly to a local file.
 * The file format is not well defined at the moment, perhaps this should be
 * changed later.
 * 
 * @author Steven Schlansker
 */
public final class FileDatabase extends Database {

	/**
	 * The file contained in this FileDatabase
	 */
	public File myFile;

	protected RandomAccessFile fd;

	protected int groupsLength;

	protected long offset;

	private final boolean storeConf;

	public FileDatabase() {
		this(true);
	}

	public FileDatabase(boolean storeConf) {
		this.storeConf = storeConf;
	}

	@Override
	public void close() {
		try {
			fd.close();
		} catch (IOException e) {
			Util.warn("Error while closing input stream for database: " + e);
		}
	}

	@Override
	public void flush() {
		// try {
		// fd.getFD().sync();
		// fd.getChannel().force(true);
		// } catch (IOException e) {
		// Util.fatalError("Error while writing to database: " + e);
		// }
	}

	@Override
	public synchronized void seek(long loc) {
		try {
			fd.seek(loc + offset);
		} catch (IOException e) {
			Util.fatalError("IO Error", e);
		}
	}

	@Override
	public synchronized void getBytes(byte[] arr, int off, int len) {
		try {
			fd.read(arr, off, len);
		} catch (IOException e) {
			Util.fatalError("IO Error", e);
		}
	}

	@Override
	public synchronized void putBytes(byte[] arr, int off, int len) {
		try {
			fd.write(arr, off, len);
		} catch (IOException e) {
			Util.fatalError("IO Error", e);
		}
	}

	@Override
	public void initialize(String loc, boolean solve) {
		try {
			myFile = new File(loc);
			if (solve) {
				fd = new RandomAccessFile(myFile, "rw");
				if (conf == null)
					Util
							.fatalError("You must specify a configuration if the database is to be created");
				if (storeConf) {
					byte[] b = conf.store();
					fd.writeInt(b.length);
					fd.write(b);
				} else
					fd.writeInt(0);
				offset = fd.getFilePointer();
				fd.setLength(offset + getByteSize());
			} else {
				fd = new RandomAccessFile(myFile, "r");
				int headerLen = fd.readInt();
				byte[] header = new byte[headerLen];
				fd.readFully(header);
				if (conf == null) {
					conf = Configuration.load(header);
				}
				offset = fd.getFilePointer();
			}
			offset -= firstByte();
		} catch (IOException e) {
			e.printStackTrace();
			Util.fatalError(e.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Util.fatalError(e.toString());
		}
	}
}
