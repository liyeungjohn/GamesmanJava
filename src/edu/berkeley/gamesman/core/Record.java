package edu.berkeley.gamesman.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map.Entry;

import edu.berkeley.gamesman.util.biginteger.BigInteger;

/**
 * Stores information about a game state
 * 
 * @author dnspies
 */
public final class Record {
	private final EnumMap<RecordFields, Long> values;

	private final EnumMap<RecordFields, Long> numStates;

	Record(Configuration conf, BigInteger state) {
		numStates = conf.getStoredFields();
		values = new EnumMap<RecordFields, Long>(RecordFields.class);
		BigInteger remainingState = state;
		for (Entry<RecordFields, Long> e : numStates.entrySet()) {
			BigInteger[] divmod = remainingState.divideAndRemainder(BigInteger
					.valueOf(e.getValue()));
			this.values.put(e.getKey(), divmod[1].longValue());
			remainingState = divmod[0];
		}
	}

	/**
	 * @param conf
	 *            The configuration object
	 * @param values
	 *            The values of each of the respective fields in this record
	 *            ordered VALUE, REMOTENESS, SCORE
	 */
	public Record(Configuration conf, long... values) {
		numStates = conf.getStoredFields();
		this.values = new EnumMap<RecordFields, Long>(RecordFields.class);
		int i = 0;
		for (Entry<RecordFields, Long> e : numStates.entrySet())
			this.values.put(e.getKey(), values[i++]);
	}

	/**
	 * @param conf
	 *            The configuration object
	 * @param pVal
	 *            Just the primitive value. All other fields are initialized to
	 *            zero.
	 */
	public Record(Configuration conf, PrimitiveValue pVal) {
		this.values = new EnumMap<RecordFields, Long>(RecordFields.class);
		numStates = conf.getStoredFields();
		for (Entry<RecordFields, Long> e : numStates.entrySet())
			this.values.put(e.getKey(), 0L);
		this.values.put(RecordFields.VALUE, (long) pVal.value());
	}

	/**
	 * @param field
	 *            The field to change
	 * @param value
	 *            The new value of the field
	 */
	public void set(RecordFields field, long value) {
		values.put(field, value);
	}

	/**
	 * @param rf
	 *            The type of one block of information
	 * @return The information encoded as a long
	 */
	public long get(RecordFields rf) {
		return values.get(rf);
	}

	/**
	 * @return The primitive value of this position
	 */
	public PrimitiveValue get() {
		return PrimitiveValue.values()[(int) get(RecordFields.VALUE)];
	}

	/**
	 * @return The integer value of this record
	 */
	public BigInteger getState() {
		BigInteger currentState = BigInteger.ZERO;
		BigInteger multiplier = BigInteger.ONE;
		for (Entry<RecordFields, Long> e : values.entrySet()) {
			currentState = currentState.add(BigInteger.valueOf(e.getValue())
					.multiply(multiplier));
			multiplier = multiplier.multiply(BigInteger.valueOf(numStates.get(e
					.getKey())));
		}
		return currentState;
	}

	/**
	 * @param conf
	 *            The configuration object
	 * @param vals
	 *            A collection of records
	 * @return The "best" record in the collection (ordered by primitive value,
	 *         then score, then remoteness)
	 */
	public static Record combine(Configuration conf, Collection<Record> vals) {
		ArrayList<Record> valsBest = new ArrayList<Record>(vals.size());
		PrimitiveValue bestPrim = PrimitiveValue.LOSE;
		for (Record val : vals) {
			PrimitiveValue pv = val.get();
			if (pv.isPreferableTo(bestPrim)) {
				valsBest.clear();
				valsBest.add(val);
				bestPrim = pv;
			} else if (pv.equals(bestPrim))
				valsBest.add(val);
		}
		EnumMap<RecordFields, Long> storedFields = conf.getStoredFields();
		if (storedFields.containsKey(RecordFields.SCORE)) {
			ArrayList<Record> valsBestScore = new ArrayList<Record>(valsBest
					.size());
			long bestScore = Long.MIN_VALUE;
			for (Record val : valsBest) {
				long score = val.get(RecordFields.SCORE);
				if (score > bestScore) {
					valsBestScore.clear();
					valsBestScore.add(val);
					bestScore = score;
				} else if (score == bestScore)
					valsBestScore.add(val);
			}
			valsBest = valsBestScore;
		}
		if (storedFields.containsKey(RecordFields.REMOTENESS)) {
			if (bestPrim.equals(PrimitiveValue.LOSE)) {
				ArrayList<Record> valsBestRemoteness = new ArrayList<Record>(
						valsBest.size());
				long bestRemoteness = 0;
				for (Record val : valsBest) {
					long remoteness = val.get(RecordFields.REMOTENESS);
					if (remoteness > bestRemoteness) {
						valsBestRemoteness.clear();
						valsBestRemoteness.add(val);
						bestRemoteness = remoteness;
					} else if (remoteness == bestRemoteness)
						valsBestRemoteness.add(val);
				}
				valsBest = valsBestRemoteness;
			} else {
				ArrayList<Record> valsBestRemoteness = new ArrayList<Record>(
						valsBest.size());
				long bestRemoteness = Long.MAX_VALUE;
				for (Record val : valsBest) {
					long remoteness = val.get(RecordFields.REMOTENESS);
					if (remoteness < bestRemoteness) {
						valsBestRemoteness.clear();
						valsBestRemoteness.add(val);
						bestRemoteness = remoteness;
					} else if (remoteness == bestRemoteness)
						valsBestRemoteness.add(val);
				}
				valsBest = valsBestRemoteness;
			}
		}
		return valsBest.get(0);
	}

	/**
	 * Changes this record to the previous position. WARNING! Does not change
	 * the score. You must do that yourself.
	 */
	public void previousPosition() {
		set(RecordFields.VALUE, get().previousMovesValue().value());
		if (values.containsKey(RecordFields.REMOTENESS))
			set(RecordFields.REMOTENESS, get(RecordFields.REMOTENESS) + 1);
	}

	@Override
	public boolean equals(Object r) {
		if (r instanceof Record) {
			Record rec = (Record) r;
			return values.equals(rec.values);
		} else
			return false;
	}

	@Override
	public String toString() {
		return values.toString();
	}

	private Record(Record record) {
		this.values = new EnumMap<RecordFields, Long>(RecordFields.class);
		for (Entry<RecordFields,Long> value : record.values.entrySet()) {
			this.values.put(value.getKey(), value.getValue());
		}
		numStates = record.numStates;
	}

	@Override
	public Record clone() {
		return new Record(this);
	}
}
