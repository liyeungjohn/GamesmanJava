package edu.berkeley.gamesman.database.filer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.Database;
import edu.berkeley.gamesman.core.RecordGroup;
import edu.berkeley.gamesman.util.DebugFacility;
import edu.berkeley.gamesman.util.Util;

/**
 * A DirectoryFilerServer opens a local DirectoryFiler and makes its services
 * available to remove clients connecting with a DirectoryFilerClient
 * 
 * @see DirectoryFiler
 * @see DirectoryFilerClient
 * @author Steven Schlansker
 */
public final class DirectoryFilerServer {

	// File root;
	ServerSocket ss;

	int port;

	String secret;

	boolean shuttingdown = false;

	protected DirectoryFiler df;

	List<Database> fds = Collections
			.synchronizedList(new ArrayList<Database>());

	List<BigInteger> locs = Collections
			.synchronizedList(new ArrayList<BigInteger>());

	/**
	 * Launch a DirectoryFilerServer and begin listening to the outside world
	 * 
	 * @param rootdir
	 *            The root of a DirectoryFiler datastore
	 * @param port
	 *            the port to listen on
	 * @param secret
	 *            a shared secret remote clients must present to connect
	 * @throws ClassNotFoundException
	 *             if the configuration failed to load
	 */
	public DirectoryFilerServer(String rootdir, int port, String secret)
			throws ClassNotFoundException {
		File root = new File(rootdir);
		if (!root.isDirectory()) {
			Util.fatalError("Root directory \"" + rootdir
					+ "\" is not a directory or could not be opened");
		}

		this.port = port;
		this.secret = secret;
		df = new DirectoryFiler(root);
	}

	/**
	 * Close down this server
	 */
	public void close() {
		try {
			ss.close();
		} catch (IOException e) {
		}
		df.close();
	}

	/**
	 * Begin listening for remote connections
	 */
	public void launchServer() {
		Socket s;
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			Util.fatalError("Could not launch server: " + e);
		}

		ThreadGroup grp = new ThreadGroup("DirectoryFilerServerThreads");

		assert Util.debug(DebugFacility.FILER,
				"Directory filer server launched on port " + port);

		try {
			while ((s = ss.accept()) != null) {
				new Thread(grp, new DirectoryFilerServerThread(s),
						"Runner for " + s).start();
			}
		} catch (IOException e) {
			if (!shuttingdown)
				Util
						.warn("Server socket unexpectedly could not accept new connections, exiting after current connections close: "
								+ e);
		}
		df.close();
	}

	private class DirectoryFilerServerThread implements Runnable {

		Socket sock;

		DataInputStream din;

		DataOutputStream dout;

		Random r = new Random();

		public DirectoryFilerServerThread(Socket s) {
			sock = s;
			try {
				din = new DataInputStream(sock.getInputStream());
				dout = new DataOutputStream(sock.getOutputStream());
			} catch (IOException e) {
				Util.warn("Connection for " + sock.getInetAddress()
						+ " caused an IOException, dropping : " + e);
			}
		}

		public void run() {
			try {
				assert Util.debug(DebugFacility.FILER,
						"Accepted filer connection");
				dout.writeInt(0x00FABFAB);
				if (din.readInt() != 0xBAFBAF00) {
					Util.warn("Dropping connection because of wrong magic");
					return;
				}

				byte[] entropy = new byte[16], secbyte, response, hash = new byte[1];

				r.nextBytes(entropy);

				dout.write(entropy);

				secbyte = secret.getBytes();

				response = new byte[din.readInt()];
				din.readFully(response);

				try {
					MessageDigest md = MessageDigest.getInstance("SHA");
					md.update(secbyte, 0, secbyte.length);
					md.update(entropy);
					hash = md.digest();
				} catch (NoSuchAlgorithmException nsa) {
					Util.fatalError("Can't SHA?");
				}

				if (!Arrays.equals(response, hash)) {
					Util.warn("Dropping connection because of wrong secret");
					sock.close();
					return;
				}

				assert Util.debug(DebugFacility.FILER,
						"Client passed authentication");

				while (true) {

					int fd;
					Database db;
					BigInteger loc;

					if (shuttingdown) {
						assert Util.debug(DebugFacility.FILER,
								"Client shutting down");
						sock.close();
						return;
					}
					byte what = din.readByte();
					switch (what) {
					case 0:
						assert Util.debug(DebugFacility.FILER,
								"Client shutting down by request");
						sock.close();
						return;
					case 1:
						assert Util.debug(DebugFacility.FILER,
								"Server shutting down");
						shuttingdown = true;
						ss.close();
						break;
					case 2:
						String[] files = df.ls();
						dout.writeInt(files.length);
						assert Util.debug(DebugFacility.FILER, "Sending "
								+ files.length + " files");
						for (String file : files) {
							dout.writeInt(file.length());
							assert Util.debug(DebugFacility.FILER,
									"Filename len is " + file.length());
							dout.write(file.getBytes());
						}
						break;
					case 3:
						// db = new FileDatabase();
						String file;
						Configuration config;
						int len = din.readInt();
						byte[] fb = new byte[len];
						din.readFully(fb);
						file = new String(fb);
						len = din.readInt();
						fb = new byte[len];
						din.readFully(fb);
						try {
							config = Configuration.load(fb);
						} catch (ClassNotFoundException e) {
							Util
									.warn("Failed to load configuration "
											+ file, e);
							break;
						}
						// db.initialize(Util.getChild(root, file).toURL()
						// .toExternalForm(), new Configuration(config));
						// //TODO: don't reference Values
						db = df.openDatabase(file, config);
						fds.add(db);
						locs.add(BigInteger.ZERO);
						dout.writeInt(fds.indexOf(db));
						assert Util.debug(DebugFacility.FILER,
								"Client opened db " + file + " for fd "
										+ fds.indexOf(db) + " with config "
										+ config);
						break;
					case 4:
						fd = din.readInt();
						db = fds.get(fd);
						db.close();
						fds.set(fd, null);
						assert Util.debug(DebugFacility.FILER, "Closed " + fd
								+ ": " + db);
						break;
					case 5:
						fd = din.readInt();
						db = fds.get(fd);
						loc = locs.get(fd);
						db.getRecordGroup(loc.longValue()).outputUnsignedBytes(
								(OutputStream) dout,
								db.getConfiguration().recordGroupByteLength);
						locs.set(fd, loc.add(BigInteger.ONE));
						break;
					case 6:
						fd = din.readInt();
						db = fds.get(fd);
						// byte val = din.readByte();
						loc = locs.get(fd);
						byte[] rawRecord = new byte[db.getConfiguration().recordGroupByteLength];
						din.read(rawRecord);
						db.putRecordGroup(loc.longValue(), new RecordGroup(db
								.getConfiguration(), rawRecord));
						locs.set(fd, loc.add(BigInteger.ONE));
						break;
					case 7:
						fd = din.readInt();
						fds.get(fd).flush();
						break;
					case 8:
						fd = din.readInt();
						len = din.readInt();
						byte[] bloc = new byte[len];
						din.readFully(bloc);
						locs.set(fd, new BigInteger(bloc));
						break;
					default:
						Util.warn("Bad IO from client");
						sock.close();
						return;
					}
				}

			} catch (IOException e) {
				e.printStackTrace(System.err);
				Util.warn("Dropping connection " + sock
						+ " because it caused an IOException: " + e);
			}
		}

	}

}
