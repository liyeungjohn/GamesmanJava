package edu.berkeley.gamesman.database;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.util.UndeterminedChunkInputStream;

public final class TransferMaster {
	public static void main(String[] args) throws IOException {
		SplitDatabase readFrom = (SplitDatabase) Database.openDatabase(args[0]);
		Configuration conf = readFrom.getConfiguration();
		SplitDatabase writeTo = new SplitDatabase(args[1], conf);
		int numSplits = Integer.parseInt(args[2]);
		int entrySize = conf.getInteger("gamesman.db.zip.entryKB", 64) << 10;
		int maxLen = 0;
		final long hashes = conf.getGame().numHashes();
		long lastForDb = 0;
		String parentFile = args[3];
		for (int i = 0; i < numSplits; i++) {
			long firstForDb = lastForDb;
			lastForDb = (i + 1) * hashes / numSplits;
			GZippedFileDatabase db = new GZippedFileDatabase(parentFile
					+ File.separator + "s" + firstForDb + ".db", conf, readFrom
					.getHeader(firstForDb, lastForDb - firstForDb));
			Scanner dbScan = new Scanner(readFrom.makeStream(firstForDb,
					lastForDb - firstForDb));
			String nextType = dbScan.next();
			while (!nextType.equals("end")) {
				String uri = dbScan.next();
				dbScan.nextLong();
				dbScan.nextLong();
				StringBuilder moveProc = new StringBuilder(maxLen);
				String[] hostFile = uri.split(":");
				String host = hostFile[0];
				String path = hostFile[1];
				String file = hostFile[2];
				if (!file.startsWith("/") && !file.startsWith(path))
					file = path + "/" + file;
				String user = null;
				if (host.contains("@")) {
					String[] userHost = host.split("@");
					user = userHost[0];
					host = userHost[1];
				}
				String command = "ssh "
						+ (user == null ? host : (user + "@" + host));
				command += " java -cp " + path + "/bin ";
				command += TransferSlave.class.getName() + " " + firstForDb
						+ " " + (lastForDb - firstForDb);
				Process p = Runtime.getRuntime().exec(command);
				PrintStream ps = new PrintStream(p.getOutputStream(), true);
				UndeterminedChunkInputStream in = new UndeterminedChunkInputStream(
						new BufferedInputStream(p.getInputStream()));
				Scanner readScan = new Scanner(in);
				String neededChunk = readScan.next();
				while (!neededChunk.equals("ready")
						|| neededChunk.equals("skip")) {
					long firstRequestedRecord = readScan.nextLong();
					long numRequestedRecords = readScan.nextLong();
					ps.println(readFrom.makeStream(firstRequestedRecord,
							numRequestedRecords));
				}
				if (neededChunk.equals("ready")) {
					ps.println("go");
					in.nextChunk();
					//TODO Finish method
				}
				nextType = dbScan.next();
			}
		}
		readFrom.close();
	}
}
