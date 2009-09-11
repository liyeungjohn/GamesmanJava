package edu.berkeley.gamesman.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import edu.berkeley.gamesman.util.Util;
import edu.berkeley.gamesman.util.biginteger.BigInteger;

/**
 * A Configuration object stores information related to a specific configuration
 * of Game, Hasher, and Records. The information should be specific enough that
 * a database will only match Configuration if the given Game and Hasher will
 * derive useful information from it.
 * 
 * @author Steven Schlansker
 */
public class Configuration {
	private Game<?> g;

	private Hasher<?> h;

	// storedFields stores a mapping of RecordFields to an integer.
	// The integer is the number of possible values of the field
	protected int[] storedFields;

	private final int[] fieldIndices = new int[RecordFields.values().length];

	/**
	 * A list of all the RecordFields used in this configuration (in the same
	 * order as stored fields)
	 */
	public final LinkedList<RecordFields> usedFields = new LinkedList<RecordFields>();

	/**
	 * 
	 */
	public final long totalStates;

	protected final BigInteger bigIntTotalStates;

	final BigInteger[] multipliers;

	final long[] longMultipliers;

	/**
	 * The number of records contained in a RecordGroup
	 */
	public final int recordsPerGroup;

	/**
	 * The number of bytes in a RecordGroup
	 */
	public final int recordGroupByteLength;

	protected final boolean recordGroupUsesLong;

	/**
	 * The database associated with this configuration
	 */
	public Database db;

	/**
	 * The properties used to create this configuration
	 */
	public final Properties props;

	/**
	 * Reads the key value pairs from the given job file into a Properties
	 * object.
	 * 
	 * @param path
	 *            A path to a job file
	 * @return A Properties object containing all the key-value pairs from the
	 *         given job file.
	 */
	public static Properties readProperties(String path) {
		Properties props = new Properties();
		addProperties(props, path);
		return props;
	}

	/**
	 * Given a Properties, will construct a Configuration
	 * 
	 * @param props
	 *            A Properties object (probably constructed from a job file).
	 * @param initLater
	 *            You must call initialize() once you have created the
	 *            appropriate Game and Hasher objects.
	 * @throws ClassNotFoundException
	 *             Could not find game class or hasher class
	 */
	public Configuration(Properties props, boolean initLater)
			throws ClassNotFoundException {
		this.props = props;
		double requiredCompression = Double.parseDouble(getProperty(
				"record.compression", "0")) / 100;
		totalStates = initializeStoredFields();
		double compression;
		if (requiredCompression == 0D)
			recordsPerGroup = 1;
		else {
			int recordGuess;
			int bitLength;
			double log2;
			log2 = Math.log(totalStates) / Math.log(2);
			if (log2 > 8) {
				recordGuess = 1;
				bitLength = (int) Math.ceil(log2);
				compression = (log2 / 8) / ((bitLength + 7) >> 3);
				while (compression < requiredCompression) {
					recordGuess++;
					bitLength = (int) Math.ceil(recordGuess * log2);
					compression = (recordGuess * log2 / 8)
							/ ((bitLength + 7) >> 3);
				}
			} else {
				bitLength = 8;
				recordGuess = (int) (8D / log2);
				compression = recordGuess * log2 / 8;
				while (compression < requiredCompression) {
					bitLength += 8;
					recordGuess = (int) (bitLength / log2);
					compression = (recordGuess * log2 / 8) / (bitLength >> 3);
				}
			}
			recordsPerGroup = recordGuess;
		}
		multipliers = new BigInteger[recordsPerGroup + 1];
		BigInteger multiplier = BigInteger.ONE;
		bigIntTotalStates = BigInteger.valueOf(totalStates);
		for (int i = 0; i <= recordsPerGroup; i++) {
			multipliers[i] = multiplier;
			multiplier = multiplier.multiply(bigIntTotalStates);
		}
		recordGroupByteLength = (bigIntTotalStates.pow(recordsPerGroup)
				.bitLength() + 7) >> 3;
		if (recordGroupByteLength < 8) {
			recordGroupUsesLong = true;
			longMultipliers = new long[recordsPerGroup + 1];
			long longMultiplier = 1;
			for (int i = 0; i <= recordsPerGroup; i++) {
				longMultipliers[i] = longMultiplier;
				longMultiplier *= totalStates;
			}
		} else {
			recordGroupUsesLong = false;
			longMultipliers = null;
		}
		if (!initLater) {
			String gamename = getProperty("gamesman.game");
			String hashname = getProperty("gamesman.hasher");
			initialize(gamename, hashname);
		}
	}

	/**
	 * Given a Properties, will construct a Configuration
	 * 
	 * @param props
	 *            A Properties object (probably constructed from a job file).
	 * @throws ClassNotFoundException
	 *             Could not find game class or hasher class
	 */
	public Configuration(Properties props) throws ClassNotFoundException {
		this(props, false);
	}

	/**
	 * Calls new Configuration(Configuration.readProperties(path))
	 * 
	 * @param path
	 *            The path to the job file to read
	 * @throws ClassNotFoundException
	 *             Could not find game class or hasher class
	 */
	public Configuration(String path) throws ClassNotFoundException {
		this(readProperties(path), false);
	}

	// To specify the bit size, use ':' followed by the number of possible
	// states
	private long initializeStoredFields() {
		String fields = getProperty("record.fields", RecordFields.VALUE.name()
				+ "," + RecordFields.REMOTENESS.name());
		RecordFields field;
		long totalStates = 1;
		int states;
		String[] splitFields = fields.split(",");
		usedFields.clear();
		storedFields = new int[splitFields.length];
		for (int i = 0; i < fieldIndices.length; i++)
			fieldIndices[i] = -1;
		for (int i = 0; i < splitFields.length; i++) {
			String[] splt = splitFields[i].split(":");
			field = RecordFields.valueOf(splt[0]);
			if (splt.length > 1)
				states = Integer.parseInt(splt[1]);
			else
				states = field.defaultNumberOfStates();
			usedFields.add(field);
			storedFields[i] = states;
			fieldIndices[field.ordinal()] = i;
			totalStates *= states;
		}
		return totalStates;
	}

	/**
	 * Initialize the Configuration with a game and a hasher object.
	 * 
	 * @param newG
	 *            The Game associated with this configuration.
	 * @param newH
	 *            The Hasher associated with this configuration.
	 */
	public void initialize(Game<?> newG, Hasher<?> newH) {
		initialize(g, h, true);
	}

	/**
	 * Initialize the Configuration with a game and a hasher object.
	 * 
	 * @param newG
	 *            The Game associated with this configuration.
	 * @param newH
	 *            The Hasher associated with this configuration.
	 * @param prepare
	 *            Whether to call prepare for the game being passed
	 */
	public void initialize(Game<?> newG, Hasher<?> newH, boolean prepare) {
		g = newG;
		h = newH;
		if (prepare) {
			g.prepare();
		}
	}

	/**
	 * Initialize the Configuration with a game and a hasher object.
	 * 
	 * @param in_gamename
	 *            The Game associated with this configuration.
	 * @param in_hashname
	 *            The Hasher associated with this configuration.
	 * @param prepare
	 *            Whether to call prepare for the game being passed
	 * @throws ClassNotFoundException
	 *             Could not load either the hasher or game class
	 */
	public void initialize(final String in_gamename, final String in_hashname,
			boolean prepare) throws ClassNotFoundException {
		String gamename = in_gamename, hashname = in_hashname;

		// Python classes end with ".py"
		if (gamename.indexOf('.') == -1) {
			gamename = "edu.berkeley.gamesman.game." + gamename;
		}
		setProperty("gamesman.game", gamename);
		if (hashname.indexOf('.') == -1) {
			hashname = "edu.berkeley.gamesman.hasher." + hashname;
		}
		setProperty("gamesman.hasher", hashname);
		this.g = Util.typedInstantiateArg(gamename, Game.class, this);
		this.h = Util.typedInstantiateArg(hashname, Hasher.class, this);
		if (prepare) {
			g.prepare();
		}
	}

	/**
	 * @param gamename
	 *            The name of the game class
	 * @param hashname
	 *            The name of the hash class
	 * @throws ClassNotFoundException
	 *             If either class is not found
	 */
	public void initialize(String gamename, String hashname)
			throws ClassNotFoundException {
		initialize(gamename, hashname, true);
	}

	/**
	 * @return the Game this configuration plays
	 */
	public Game<?> getGame() {
		return g;
	}

	/**
	 * Specify which fields are to be saved by the database Each Field maps to
	 * an integer representing the number of possible values for that field
	 * 
	 * @param fields
	 *            EnumMap as described above
	 */
	public void setStoredFields(EnumMap<RecordFields, Integer> fields) {
		storedFields = new int[fields.size()];
		usedFields.clear();
		int i;
		for (i = 0; i < fieldIndices.length; i++) {
			fieldIndices[i] = -1;
		}
		i = 0;
		for (Entry<RecordFields, Integer> e : fields.entrySet()) {
			fieldIndices[e.getKey().ordinal()] = i;
			storedFields[i] = e.getValue();
			usedFields.add(e.getKey());
			i++;
		}
	}

	/**
	 * Unserialize a configuration from a bytestream
	 * 
	 * @param barr
	 *            Bytes to deserialize
	 * @return a Configuration
	 * @throws ClassNotFoundException
	 *             Could not find game class or hasher class
	 */
	public static Configuration load(byte[] barr) throws ClassNotFoundException {
		try {
			DataInputStream instream = new DataInputStream(
					new ByteArrayInputStream(barr));
			Properties props = new Properties();

			byte[] t = new byte[instream.readInt()];
			instream.readFully(t);
			ByteArrayInputStream bin = new ByteArrayInputStream(t);
			props.load(bin);
			Configuration conf = new Configuration(props, true);

			// assert Util.debug(DebugFacility.CORE, "Deserialized
			// properties:\n"+props);

			EnumMap<RecordFields, Integer> sf = new EnumMap<RecordFields, Integer>(
					RecordFields.class);

			String gamename = instream.readUTF();
			String hashername = instream.readUTF();

			int num = instream.readInt();

			// assert Util.debug(DebugFacility.CORE, "Expecting "+num+" stored
			// fields");

			for (int i = 0; i < num; i++) {
				String name = instream.readUTF();
				// assert Util.debug(DebugFacility.CORE," Found field "+name);
				sf.put(RecordFields.valueOf(name), (int) instream.readLong());
			}
			conf.setStoredFields(sf);

			try {
				conf.g = Util.typedInstantiateArg(gamename, Game.class, conf);
			} catch (ClassNotFoundException e) {
				conf.g = Util.typedInstantiateArg(conf
						.getProperty("gamesman.game"), Game.class, conf);
			}
			try {
				conf.h = Util.typedInstantiateArg(hashername, Hasher.class,
						conf);
			} catch (ClassNotFoundException e) {
				conf.h = Util.typedInstantiateArg(conf
						.getProperty("gamesman.hasher"), Hasher.class, conf);
			}

			conf.initialize(conf.g, conf.h);

			return conf;
		} catch (IOException e) {
			Util.fatalError(
					"Could not resuscitate Configuration from bytes :(\n"
							+ new String(barr), e);
		}
		return null;
	}

	/**
	 * Return a serialized version of the Configuration suitable for storing
	 * persistently
	 * 
	 * @return a String with the Configuration information
	 */
	public byte[] store() {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);

			ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

			props.store(baos2, null);

			out.writeInt(baos2.size());
			out.write(baos2.toByteArray());

			baos2.close();

			out.writeUTF(g.getClass().getCanonicalName());
			out.writeUTF(h.getClass().getCanonicalName());

			out.writeInt(storedFields.length);

			for (RecordFields rf : usedFields) {
				out.writeUTF(rf.name());
				out.writeLong(getFieldStates(rf));
			}

			out.close();

			return baos.toByteArray();
		} catch (IOException e) {
			Util.fatalError("IO Exception shouldn't have happened here", e);
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Configuration))
			return false;
		Configuration c = (Configuration) o;
		return c.props.equals(props) && c.g.getClass().equals(g.getClass())
				&& c.h.getClass().equals(h.getClass());
	}

	/**
	 * @return the Hasher this Configuration is using
	 */
	public Hasher<?> getHasher() {
		return h;
	}

	public String toString() {
		return "Config[" + props + "," + g + "," + h + ","
				+ Arrays.toString(storedFields) + "]";
	}

	/**
	 * Get a property by its name
	 * 
	 * @param key
	 *            the name of the configuration property
	 * @return its value
	 */
	public String getProperty(String key) {
		String s = props.getProperty(key);
		if (s == null)
			Util
					.fatalError("Property " + key
							+ " is unset and has no default!");
		return s;
	}

	/**
	 * For python compatibility.
	 * 
	 * @param key
	 *            the name of the configuration property
	 * @return its value
	 * @see #getProperty(String)
	 */
	public String __getitem__(String key) {
		return getProperty(key);
	}

	/**
	 * For python compatibility. Returns false if key does not exist.
	 * 
	 * @param key
	 *            The key to check if it's in the configuration
	 * @return is the key in the configuration?
	 * @see #getProperty(String)
	 */
	public boolean __contains__(String key) {
		return props.containsKey(key);
	}

	/**
	 * Parses a property as a boolean.
	 * 
	 * @param key
	 *            the name of the configuration property
	 * @param dfl
	 *            default value
	 * @return false iff s is "false" or s is "0", ignoring case
	 */
	public boolean getBoolean(String key, boolean dfl) {
		String s = props.getProperty(key);
		if (s != null) {
			try {
				return !s.equalsIgnoreCase("false") && !s.equalsIgnoreCase("0");
			} catch (Exception e) {
			}
		}
		return dfl;
	}

	/**
	 * Parses a property as an Integer.
	 * 
	 * @param key
	 *            the name of the configuration property
	 * @param dfl
	 *            default value
	 * @return The value associated with the key, if defined and an integer.
	 *         Otherwise, returns dfl.
	 */
	public int getInteger(String key, int dfl) {
		String value = props.getProperty(key);
		if (value == null)
			return dfl;
		else
			return Integer.parseInt(value);

	}

	/**
	 * Parses a property as a Long.
	 * 
	 * @param key
	 *            the name of the configuration property
	 * @param dfl
	 *            default value
	 * @return The value associated with the key, if defined and an long.
	 *         Otherwise, returns dfl.
	 */
	public Long getLong(String key, Long dfl) {
		try {
			return Long.parseLong(props.getProperty(key));
		} catch (Exception e) {
		}
		return dfl;
	}

	/**
	 * Parses a property as an array of Integers separated by the regex ", *"
	 * 
	 * @param key
	 *            the name of the configuration property
	 * @param dfl
	 *            default value
	 * @return The value associated with the key, if defined and an integer
	 *         array. Otherwise, returns dfl.
	 */
	public Integer[] getIntegers(String key, Integer[] dfl) {
		try {
			return Util.parseIntegers(props.getProperty(key).split(", *"));
		} catch (Exception e) {
		}
		return dfl;
	}

	/**
	 * Get a property by its name. If the property is not set, return dfl
	 * 
	 * @param key
	 *            the name of the configuration property
	 * @param dfl
	 *            default value
	 * @return its value
	 */
	public String getProperty(String key, String dfl) {
		return props.getProperty(key, dfl);
	}

	/**
	 * Set a property by its name
	 * 
	 * @param key
	 *            the name of the configuration property to set
	 * @param value
	 *            the new value
	 * @return the old value
	 */
	public Object setProperty(String key, String value) {
		return props.setProperty(key, value);
	}

	/**
	 * Read a list of properties from a file The properties should be specified
	 * as key = value pairs. Blank lines are ignored.
	 * 
	 * @param path
	 *            The file path to open
	 */
	private static void addProperties(Properties props, String path) {
		LineNumberReader r = null;
		try {
			r = new LineNumberReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			Util.fatalError("Could not open property file", e);
		}
		String line;
		try {
			while ((line = r.readLine()) != null) {
				line = line.trim();
				if (line.equals("") || line.charAt(0) == '#')
					continue;
				String[] arr = line.split("=", 2); // semantics are slightly
				// different from python
				// The following line can't be here because it causes the Util
				// class to be loaded before
				// we're ready for it.
				// Util.assertTrue(arr.length == 2, "Malformed property file at
				// line \""+line+"\"");
				props.setProperty(arr[0].trim(), arr[1].trim());
			}
		} catch (IOException e) {
			Util.fatalError("Could not read from property file", e);
		}
	}

	/**
	 * Add all the properties into this configuration. Property strings should
	 * be split by = signs.
	 * 
	 * @param propStrings
	 *            the list of properties to set.
	 */
	public void addProperties(ArrayList<String> propStrings) {
		for (String line : propStrings) {
			if (line.equals(""))
				continue;
			String[] arr = line.split("\\s+=\\s+");
			Util.assertTrue(arr.length == 2,
					"Malformed property file at line \"" + line + "\"");
			setProperty(arr[0], arr[1]);
		}
	}

	/**
	 * @return all keys in the configuration
	 */
	public Set<Object> getKeys() {
		return props.keySet();
	}

	/**
	 * Remove a key from the configuration
	 * 
	 * @param key
	 *            the key to remove
	 */
	public void deleteProperty(String key) {
		props.remove(key);
	}

	private static BufferedReader in = new BufferedReader(
			new InputStreamReader(System.in));

	/**
	 * Return a property, prompting the user if it doesn't exist
	 * 
	 * @param key
	 *            the key to get
	 * @return its value
	 */
	public String getPropertyWithPrompt(String key) {
		String s = props.getProperty(key);
		if (s == null) {
			System.out
					.print("Gamesman would like you to specify the value for '"
							+ key + "'\n\t>");
			System.out.flush();
			try {
				return in.readLine();
			} catch (IOException e) {
				Util.fatalError("Could not read a line from console", e);
				return null;
			}
		}
		return s;
	}

	/**
	 * @return the Database used to store this particular solve
	 * @throws ClassNotFoundException
	 *             Could not load the database class
	 */
	public Database openDatabase() throws ClassNotFoundException {
		if (db != null)
			return db;
		String[] dbType = getProperty("gamesman.database").split(":");
		if (dbType.length > 1 && dbType[0].trim().equals("cached")) {
			db = Util.typedInstantiate("edu.berkeley.gamesman.database."
					+ dbType[1], Database.class);
		} else {
			db = Util.typedInstantiate("edu.berkeley.gamesman.database."
					+ dbType[0], Database.class);
		}
		db.initialize(getProperty("gamesman.db.uri"), this);
		return db;
	}

	/**
	 * @param rf
	 *            The field
	 * @return Whether or not this record configuration contains rf
	 */
	public boolean containsField(RecordFields rf) {
		return fieldIndices[rf.ordinal()] >= 0;
	}

	/**
	 * @param rf
	 *            The field
	 * @return The number of possible states for rf
	 */
	public int getFieldStates(RecordFields rf) {
		return storedFields[getFieldIndex(rf)];
	}

	/**
	 * @param rf
	 *            The field
	 * @return The index in a record array that corresponds to rf
	 */
	public int getFieldIndex(RecordFields rf) {
		return fieldIndices[rf.ordinal()];
	}

	/**
	 * @return The number of fields this configuration contains
	 */
	public int numFields() {
		return storedFields.length;
	}
}