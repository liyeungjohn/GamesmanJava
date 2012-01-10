package edu.berkeley.gamesman.propogater.tree.node;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;

import edu.berkeley.gamesman.propogater.common.Combinable;
import edu.berkeley.gamesman.propogater.factory.Factory;
import edu.berkeley.gamesman.propogater.tree.Tree;
import edu.berkeley.gamesman.propogater.writable.ParentPair;
import edu.berkeley.gamesman.propogater.writable.ValueWrapper;
import edu.berkeley.gamesman.propogater.writable.WritableSettable;
import edu.berkeley.gamesman.propogater.writable.WritableSettableComparable;
import edu.berkeley.gamesman.propogater.writable.list.WritableArray;
import edu.berkeley.gamesman.propogater.writable.list.WritableList;

public final class TreeNode<KEY extends WritableSettableComparable<KEY>, VALUE extends WritableSettable<VALUE>>
		implements WritableSettable<TreeNode<KEY, VALUE>>,
		Combinable<TreeNode<KEY, VALUE>>, Configurable {
	private WritableList<ParentPair<KEY>> parents;
	private ValueWrapper<VALUE> myValue;
	private WritableArray<VALUE> children;
	private Configuration conf;

	public TreeNode() {
	}

	public TreeNode(Configuration conf) {
		setConf(conf);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		parents.write(out);
		myValue.write(out);
		children.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		parents.readFields(in);
		myValue.readFields(in);
		children.readFields(in);
	}

	@Override
	public void set(TreeNode<KEY, VALUE> t) {
		parents.set(t.parents);
		myValue.set(t.myValue);
		children.set(t.children);
	}

	public boolean makeInitial(Tree<KEY, VALUE> tree, KEY place) {
		children.clear();
		return tree.getInitialValue(place, myValue.setHasAndGet());
	}

	@Override
	public void combineWith(TreeNode<KEY, VALUE> other) {
		parents.addAll(other.parents);
		children.merge(other.children, isMain());
		// Careful not to do these in the wrong order since isMain() depends on
		// myValue
		if (myValue.hasValue()) {
			assert !other.myValue.hasValue();
		} else {
			myValue.set(other.myValue);
		}
	}

	public boolean isMain() {
		return myValue.hasValue();
	}

	public void clear() {
		parents.clear();
		myValue.clear();
		children.clear();
	}

	public boolean seenParent(int i) {
		return parents.get(i).seen();
	}

	public void setParent(int i, KEY key) {
		clear();
		parents.add().set(i, key);
	}

	public KEY getParentKey(int i) {
		return parents.get(i).getKey();
	}

	public int numParents() {
		return parents.length();
	}

	public void setNumChildren(int numChildren) {
		children.setLength(numChildren);
	}

	public VALUE getValue() {
		return myValue.get();
	}

	public boolean isNew() {
		return !myValue.hasValue();
	}

	public boolean combine(Tree<KEY, VALUE> tree) {
		assert myValue.hasValue();
		return tree.combine(children, myValue.get());
	}

	public void toParent(Tree<KEY, VALUE> tree, KEY childKey,
			TreeNode<KEY, VALUE> childNode, int parentNum) {
		clear();
		ParentPair<KEY> pair = childNode.parents.get(parentNum);
		int childNum = pair.getInt();
		children.setLength(childNum + 1);
		tree.travelUp(childNode.getValue(), childNum, childKey, pair.getKey(),
				children.setHasAndGet(childNum));
	}

	private static <KEY extends WritableSettableComparable<KEY>> Factory<ParentPair<KEY>> makePairFactory(
			final Class<KEY> keyClass, final Configuration conf) {
		return new Factory<ParentPair<KEY>>() {
			@Override
			public ParentPair<KEY> create() {
				return new ParentPair<KEY>(keyClass, conf);
			}
		};
	}

	@Override
	public String toString() {
		return myValue + ", Parents: " + parents + ", Children: " + children;
	}

	@Override
	public void setConf(Configuration conf) {
		Class<KEY> keyClass = Tree.<KEY> getRunKeyClass(conf);
		Class<VALUE> valClass = Tree.<VALUE> getRunValueClass(conf);
		parents = new WritableList<ParentPair<KEY>>(makePairFactory(keyClass,
				conf));
		myValue = new ValueWrapper<VALUE>(valClass, conf);
		children = new WritableArray<VALUE>(valClass, conf);
		this.conf = conf;
	}

	@Override
	public Configuration getConf() {
		return conf;
	}
}
