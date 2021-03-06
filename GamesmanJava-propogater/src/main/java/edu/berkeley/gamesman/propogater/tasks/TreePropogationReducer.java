package edu.berkeley.gamesman.propogater.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import edu.berkeley.gamesman.propogater.common.ConfParser;
import edu.berkeley.gamesman.propogater.tree.Tree;
import edu.berkeley.gamesman.propogater.tree.TreeNode;
import edu.berkeley.gamesman.propogater.writable.Entry;
import edu.berkeley.gamesman.propogater.writable.IntEntry;
import edu.berkeley.gamesman.propogater.writable.list.WritableList;

public class TreePropogationReducer<K extends WritableComparable<K>, V extends Writable, PI extends Writable, UM extends Writable, CI extends Writable, DM extends Writable>
		extends TreeReducer<K, V, PI, UM, CI, DM> {
	private Tree<K, V, PI, UM, CI, DM> tree;
	private final HashSet<IntWritable> changed = new HashSet<IntWritable>();
	private final HashMap<IntWritable, LongWritable> recordCount = new HashMap<IntWritable, LongWritable>();
	private IntWritable div = new IntWritable();

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		tree = ConfParser.<K, V, PI, UM, CI, DM> newTree(conf);
	}

	@Override
	protected void combine(K key, TreeNode<K, V, PI, UM, CI, DM> value,
			int division) {
		if (!value.hasValue()) {
			throw new RuntimeException(
					"No value found at too late a stage: key = \n"
							+ key.toString());
		}
		WritableList<IntEntry<UM>> upList = value.getUpList();
		if (!upList.isEmpty()) {
			div.set(division);
			if (changed.add(div))
				div = new IntWritable();
			WritableList<Entry<K, CI>> childList = value.getChildren();
			for (int i = 0; i < upList.length(); i++) {
				IntEntry<UM> mess = upList.get(i);
				Entry<K, CI> child = childList.get(mess.getKey());
				if (tree.copyUM()) {
					mess.swapValues((Entry) child);
				} else
					tree.receiveUp(key, value.getValue(), child.getKey(),
							mess.getValue(), child.getValue());
			}
			upList.clear();
		}
	}

	@Override
	protected void cleanup(Context context) {
		super.cleanup(context);
		if (!changed.isEmpty()) {
			for (IntWritable i : changed) {
				context.getCounter("needs_propogation", "t" + i);
			}
		}
	}
}
