package edu.berkeley.gamesman.database;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.util.UndeterminedChunkOutputStream;

public final class TransferSlave {
	public static void main(String[] args) throws IOException {
		GZippedFileDatabase db = (GZippedFileDatabase) Database
				.openDatabase(args[0]);
		long firstRecord = Long.parseLong(args[1]);
		long numRecords = Long.parseLong(args[2]);
		Configuration conf = db.getConfiguration();
		long gameLength = conf.getGame().numHashes();
		Scanner scan = new Scanner(System.in);
		UndeterminedChunkOutputStream out = new UndeterminedChunkOutputStream(
				System.out);
		PrintStream printOut = new PrintStream(out, true);
		DistributedDatabase allRecords = new DistributedDatabase(conf, db
				.getHeader(0, gameLength), scan, printOut);
		GZippedFileDatabase.GZipHandle dh = db.getHandle();
		long numBytes = db.prepareMoveRange(dh, firstRecord, numRecords,
				allRecords);
		allRecords.close();
		if (numBytes == 0) {
			printOut.println("skip");
			printOut.close();
		} else {
			printOut.println("ready");
			printOut.flush();
			out.nextChunk();
			out.flush();
			while (!scan.nextLine().equals("go"))
				;
			byte[] arr = new byte[(int) Math.min(ReadZippedRecords.BUFFER_SIZE,
					numBytes)];
			while (numBytes > 0) {
				int bytesRead = db.getZippedBytes(dh, arr, 0,
						ReadZippedRecords.BUFFER_SIZE);
				out.write(arr, 0, bytesRead);
				out.flush();
				numBytes -= bytesRead;
			}
			out.close();
		}
		db.close();
	}
}
