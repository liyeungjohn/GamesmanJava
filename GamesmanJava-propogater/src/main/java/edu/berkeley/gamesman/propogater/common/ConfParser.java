package edu.berkeley.gamesman.propogater.common;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.util.ReflectionUtils;

import edu.berkeley.gamesman.propogater.solver.Tier;
import edu.berkeley.gamesman.propogater.tree.Tree;

public class ConfParser {
	public static final String TIER_FOLDER_FORMAT = "t%03d";
	public static final String DATA_FOLDER = "data";
	public static final String COMBINE_FOLDER = "combine";
	public static final String CP_FORMAT = "c%03d";
	public static final String OUTPUT_FOLDER = "output";
	public static final String TEMP_FOLDER = "temp";

	public static final String EXTENSION_PREF = ".t";
	public static final String EXTENSION_FORMAT = EXTENSION_PREF + "%03d";

	// public static final String tempPref = "temp";
	// public static final String tempFormat = tempPref + "%03d";
	public static final String CREATION_JOB_FORMAT = "Tier %d Creation";
	public static final String PROPOGATION_JOB_FORMAT = "Tier %d Propogation";
	public static final String COMBINATION_JOB_FORMAT = "Tier %d Combination";

	public static <K extends WritableComparable<K>, V extends Writable, PI extends Writable, PM extends Writable, CI extends Writable, CM extends Writable> Tree<K, V, PI, PM, CI, CM> newTree(
			Configuration conf) {
		return ReflectionUtils.newInstance(
				ConfParser.<K, V, PI, PM, CI, CM> getTreeClass(conf), conf);
	}

	public static <K extends WritableComparable<K>, V extends Writable, PI extends Writable, PM extends Writable, CI extends Writable, CM extends Writable> Class<? extends Tree<K, V, PI, PM, CI, CM>> getTreeClass(
			Configuration conf) {
		@SuppressWarnings("unchecked")
		Class<? extends Tree<K, V, PI, PM, CI, CM>> c = (Class<? extends Tree<K, V, PI, PM, CI, CM>>) conf
				.<Tree> getClass("propogater.tree.class", null, Tree.class);
		if (c == null)
			throw new RuntimeException("Tree class not set");
		else
			return c;
	}

	public static Path getTierPath(Configuration conf, int num) {
		return new Path(getWorkPath(conf), String.format(TIER_FOLDER_FORMAT,
				num));
	}

	public static Path getWorkPath(Configuration conf) {
		return new Path(conf.get("propogation.work.path", "work"));
	}

	public static void setDivision(Configuration conf, int num) {
		conf.setInt("propogater.working.division", num);
	}

	public static int getDivision(Configuration conf) {
		int result = conf.getInt("propogater.working.division", -1);
		if (result == -1)
			throw new RuntimeException("division not set");
		return result;
	}

	public static Path getOutputPath(Configuration conf) {
		return new Path(conf.get("propogater.out.path", "out"));
	}

	public static Set<IntWritable> getWorkingSet(Configuration conf) {
		return makeIntSet(conf.get("propogater.working.set"));
	}

	public static void setWorkingSet(Configuration conf, Set<Tier> cycleSet) {
		conf.set("propogater.working.set", makeString(cycleSet));
	}

	private static String makeString(Set<Tier> cycleSet) {
		StringBuilder s = new StringBuilder();
		boolean first = true;
		for (Tier t : cycleSet) {
			if (first)
				first = false;
			else
				s.append(",");
			s.append(t.num);
		}
		return s.toString();
	}

	private static Set<IntWritable> makeIntSet(String s) {
		HashSet<IntWritable> result = new HashSet<IntWritable>();
		String[] stringRes = s.split(",");
		for (int i = 0; i < stringRes.length; i++) {
			result.add(new IntWritable(Integer.parseInt(stringRes[i])));
		}
		return result;
	}

	public static void setOutputPath(Configuration conf, Path p) {
		conf.set("propogater.out.path", p.toString());
	}

	public static void setWorkingPath(Configuration conf, Path path) {
		conf.set("propogation.work.path", path.toString());
	}

	public static FileSystem getWorkFileSystem(Configuration conf)
			throws IOException {
		return getWorkPath(conf).getFileSystem(conf);
	}

	public static void addParameters(Configuration conf, Path p, boolean andSend)
			throws IOException {
		Properties props = getProperties(conf, p);
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			conf.set(key, value);
		}
		if (andSend)
			sendParameters(conf, props);
	}

	private static Properties getProperties(Configuration conf, Path p)
			throws IOException {
		Properties props = new Properties();
		FileSystem fs = p.getFileSystem(conf);
		FSDataInputStream is = fs.open(p);
		props.load(is);
		is.close();
		return props;
	}

	public static void sendParameters(Configuration conf, Properties props)
			throws IOException {
		Path outParPath = getOutputParametersPath(conf);
		if (outParPath != null) {
			FileSystem oppFS = outParPath.getFileSystem(conf);
			if (oppFS.exists(outParPath)) {
				props.putAll(getProperties(conf, outParPath));
				IOCheckOperations.delete(oppFS, outParPath, false);
			}
			OutputStream os = oppFS.create(outParPath);
			props.store(os, "");
			os.close();
		}
	}

	public static <K, V> Class<? extends Partitioner<K, V>> getCleanupPartitionerClass(
			Configuration conf) {
		return (Class<Partitioner<K, V>>) conf.getClass(
				"propogater.cleanup.partitioner", null, Partitioner.class);
	}

	public static Path getOutputParametersPath(Configuration conf) {
		String pString = conf.get("propogater.output.parameters");
		if (pString == null)
			return null;
		else
			return new Path(pString);
	}

	public static void addOutputParameter(Configuration conf, String key,
			String value) throws IOException {
		Properties props = new Properties();
		props.setProperty(key, value);
		sendParameters(conf, props);
	}

	public static <KEY extends WritableComparable, VALUE extends Writable> Partitioner<KEY, VALUE> getPartitionerInstance(
			Configuration conf) {
		return ReflectionUtils.newInstance(
				ConfParser.<KEY, VALUE> getCleanupPartitionerClass(conf), conf);
	}
}
