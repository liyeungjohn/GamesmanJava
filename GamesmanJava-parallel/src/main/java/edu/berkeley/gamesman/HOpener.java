package edu.berkeley.gamesman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.thrift.TException;

import edu.berkeley.gamesman.propogater.common.ConfParser;
import edu.berkeley.gamesman.propogater.tree.Tree;
import edu.berkeley.gamesman.propogater.writable.WritableSettable;
import edu.berkeley.gamesman.propogater.writable.WritableSettableComparable;
import edu.berkeley.gamesman.solve.reader.SolveReader;
import edu.berkeley.gamesman.solve.reader.SolveReaders;
import edu.berkeley.gamesman.thrift.GamestateResponse;
import edu.berkeley.gamesman.util.Pair;
import edu.berkeley.gamesman.game.tree.GameTree;
import edu.berkeley.gamesman.game.type.GameRecord;
import edu.berkeley.gamesman.hadoop.ranges.Range;
import edu.berkeley.gamesman.hadoop.ranges.RangeRecords;
import edu.berkeley.gamesman.hadoop.ranges.RangeTree;
import edu.berkeley.gamesman.hasher.genhasher.GenState;

public class HOpener implements Opener {

	private class GFetcher<S extends GenState> implements RecordFetcher {
		private final RangeTree<S> tree;
		private final Path folderPath;
		private final SolveReader<S> reader;
		private final boolean solved;
		private final Partitioner<Range<S>, RangeRecords> partitioner;

		public GFetcher(Configuration hConf, String game, String filename)
				throws ClassNotFoundException, IOException {
			this.tree = (RangeTree<S>) ConfParser
					.<Range<S>, RangeRecords> newTree(hConf);
			String folderName = hConf.get("solve.folder");
			if (folderName == null) {
				folderPath = new Path(solveDirectory, filename + "_folder");
			} else
				folderPath = new Path(folderName);
			solved = folderPath.getFileSystem(hConf).exists(folderPath);
			reader = SolveReaders.<S> get(hConf, game);
			if (solved)
				partitioner = ConfParser
						.<Range<S>, RangeRecords> getPartitionerInstance(hConf);
			else
				partitioner = null;
		}

		@Override
		public List<GamestateResponse> getNextMoveValues(String board)
				throws TException {
			S position = reader.getPosition(board);
			Collection<Pair<String, S>> children = reader.getChildren(position);
			ArrayList<GamestateResponse> records = new ArrayList<GamestateResponse>(
					children.size());
			for (Pair<String, S> child : children) {
				GamestateResponse response = getMoveValue(child.cdr, true);
				response.setMove(child.car);
				records.add(response);
			}
			return records;
		}

		@Override
		public GamestateResponse getMoveValue(String board) throws TException {
			S position = reader.getPosition(board);
			return getMoveValue(position, false);
		}

		private GamestateResponse getMoveValue(S position,
				boolean previousPosition) {
			GamestateResponse response = new GamestateResponse();
			response.setBoard(reader.getString(position));
			if (solved) {
				GameRecord rec;
				try {
					Range<S> posRange = tree.makeContainingRange(position);
					RangeRecords recs = SolveReaders
							.<Range<S>, RangeRecords> readPosition(tree,
									folderPath, posRange, partitioner);
					rec = tree.getRecord(posRange, position, recs);
					if (previousPosition)
						rec.previousPosition();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				response.setValue(rec.getValue().name().toLowerCase());
				response.setRemoteness(rec.getRemoteness());
			}
			return response;
		}

	}

	private class HFetcher<KEY extends WritableSettableComparable<KEY>>
			implements RecordFetcher {
		private final GameTree<KEY> tree;
		private final Path folderPath;
		private final SolveReader<KEY> reader;
		private final boolean solved;
		private final Partitioner<KEY, GameRecord> partitioner;

		public HFetcher(Configuration hConf, String game, String filename)
				throws ClassNotFoundException, IOException {
			this.tree = (GameTree<KEY>) ConfParser
					.<KEY, GameRecord> newTree(hConf);
			String folderName = hConf.get("solve.folder");
			if (folderName == null) {
				folderPath = new Path(solveDirectory, filename + "_folder");
			} else
				folderPath = new Path(folderName);
			solved = folderPath.getFileSystem(hConf).exists(folderPath);
			reader = SolveReaders.<KEY> get(hConf, game);
			if (solved)
				partitioner = ConfParser
						.<KEY, GameRecord> getPartitionerInstance(hConf);
			else
				partitioner = null;
		}

		@Override
		public List<GamestateResponse> getNextMoveValues(String board)
				throws TException {
			KEY position = reader.getPosition(board);
			Collection<Pair<String, KEY>> children = reader
					.getChildren(position);
			ArrayList<GamestateResponse> records = new ArrayList<GamestateResponse>(
					children.size());
			for (Pair<String, KEY> child : children) {
				GamestateResponse response = getMoveValue(child.cdr, true);
				response.setMove(child.car);
				records.add(response);
			}
			return records;
		}

		@Override
		public GamestateResponse getMoveValue(String board) throws TException {
			KEY position = reader.getPosition(board);
			return getMoveValue(position, false);
		}

		private GamestateResponse getMoveValue(KEY position,
				boolean previousPosition) {
			GamestateResponse response = new GamestateResponse();
			response.setBoard(reader.getString(position));
			if (solved) {
				GameRecord rec;
				try {
					rec = SolveReaders.<KEY, GameRecord> readPosition(tree,
							folderPath, position, partitioner);
					if (previousPosition)
						rec.previousPosition();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				response.setValue(rec.getValue().name().toLowerCase());
				response.setRemoteness(rec.getRemoteness());
			}
			return response;
		}

	}

	private final Configuration conf;
	private final Path solveDirectory;
	private final FileSystem fs;

	public HOpener(Configuration conf, Path solveDirectory) throws IOException {
		this.conf = conf;
		this.solveDirectory = solveDirectory;
		fs = solveDirectory.getFileSystem(conf);
	}

	@Override
	public RecordFetcher addDatabase(Map<String, String> params, String game,
			String filename) {
		Properties props = new Properties();
		Path solvePath = new Path(solveDirectory, filename + ".job");
		try {
			InputStream is;
			if (fs.exists(solvePath)) {
				is = fs.open(solvePath);
			} else {
				is = fs.open(new Path(solveDirectory, game + ".job"));
			}
			props.load(is);
			is.close();
			Configuration hConf = new Configuration(conf);
			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				hConf.set(entry.getKey().toString(), entry.getValue()
						.toString());
			}
			Class<? extends Tree<?, ?>> c = ConfParser
					.<WritableSettableComparable, WritableSettable> getTreeClass(hConf);
			if (RangeTree.class.isAssignableFrom(c))
				return new GFetcher(hConf, game, filename);
			else
				return new HFetcher(hConf, game, filename);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
