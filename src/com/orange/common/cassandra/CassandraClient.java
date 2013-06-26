package com.orange.common.cassandra;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.orange.common.utils.PropertyUtil;
import org.apache.log4j.Logger;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.CountQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;

public class CassandraClient {

    static Logger log = Logger.getLogger(CassandraClient.class.getName());

	Cluster cluster;
	Keyspace keyspace;

	final static StringSerializer ss = StringSerializer.get();
	final static LongSerializer ls = LongSerializer.get();
	final static UUIDSerializer us = UUIDSerializer.get();
	final static int UNLIMITED_COUNT = 99999;

	final static int MAX_COUNT_FOR_MULTI_ROW = 50;

    private static CassandraClient ourInstance = new CassandraClient();

    public static CassandraClient getInstance() {
        return ourInstance;
    }

    private CassandraClient() {
        String address = PropertyUtil.getStringProperty("cassandra.address", "127.0.0.1");
        String port = PropertyUtil.getStringProperty("cassandra.port", "9160");
        String clusterName = PropertyUtil.getStringProperty("cassandra.cluster", "game_cluster");
        String keyspace = PropertyUtil.getStringProperty("cassandra.keyspace", "game");

        initServer(address+":"+port, clusterName);
        initKeyspace(keyspace);
    }

    /*
	public CassandraClient(String serverNameAndPort, String clusterName,
			String keyspaceName) {
		this.initServer(serverNameAndPort, clusterName);
		this.initKeyspace(keyspaceName);

		assert (cluster != null && keyspace != null);
	}
	*/

	public void initServer(String serverNameAndPort, String clusterName) { // e.g.
		// "localhost:9160"
		cluster = HFactory.getOrCreateCluster(clusterName,
				new CassandraHostConfigurator(serverNameAndPort));

        log.info("<cassandra> cluster "+clusterName+" @"+serverNameAndPort);
	}

	public void initKeyspace(String keyspaceName) {
		if (cluster == null) {
			return;
		}
		keyspace = HFactory.createKeyspace(keyspaceName, cluster);
        log.info("<cassanrda> keyspace is "+keyspaceName);
	}

	public boolean insert(String columnFamilyName, String key,
			String columnName, String columnValue) {
		Mutator<String> mutator = HFactory.createMutator(keyspace,
				StringSerializer.get());
		mutator.addInsertion(key, columnFamilyName, HFactory.createStringColumn(columnName, columnValue));
		mutator.execute();
        log.info("<cassandra> insert "+columnFamilyName+", key="+key+", column="+columnName+", value="+columnValue);
		return true;
	}

	public boolean insert(String columnFamilyName, String key,
			String[] columnNames, String[] columnValues) {
		Mutator<String> mutator = HFactory.createMutator(keyspace,
				StringSerializer.get());
		int len = columnNames.length;
		for (int i = 0; i < len; i++) {
			String columnName = columnNames[i];
			String columnValue = "";
			if (columnValues != null && columnValues[i] != null)
				columnValue = columnValues[i];
			mutator.addInsertion(key, columnFamilyName, HFactory
					.createStringColumn(columnName, columnValue));
		}
		mutator.execute();
		return true;
	}

	public boolean insert(String columnFamilyName, String key,
			UUID[] columnNames, String[] columnValues) {
		Mutator<String> mutator = HFactory.createMutator(keyspace,
				StringSerializer.get());
		int len = columnNames.length;
		for (int i = 0; i < len; i++) {

			UUID columnName = columnNames[i];
			String columnValue = "";
			if (columnValues != null && columnValues[i] != null)
				columnValue = columnValues[i];

			HColumn<UUID, String> column = HFactory.createColumn(columnName,
					columnValue, us, ss);
			mutator.addInsertion(key, columnFamilyName, column);
		}
		mutator.execute();
		return true;
	}

	public boolean insert(String columnFamilyName, String key,
			Map<String, String> columnValueMap) {

		Mutator<String> mutator = HFactory.createMutator(keyspace,
				StringSerializer.get());
		for (Map.Entry<String, String> entry : columnValueMap.entrySet()) {
			String columnName = entry.getKey();
			String columnValue = entry.getValue();
			if (columnValue == null) {
				mutator.addInsertion(key, columnFamilyName, HFactory
						.createStringColumn(columnName, ""));
			} else {
				mutator.addInsertion(key, columnFamilyName, HFactory
						.createStringColumn(columnName, columnValue));
			}
		}

		mutator.execute();
		return true;
	}

	public boolean insert(String columnFamilyName, String key, UUID columnName,
			String columnValue) {
		Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
		HColumn<UUID, String> column = HFactory.createColumn(columnName,
				columnValue, us, ss);
		mutator.insert(key, columnFamilyName, column);
		return true;
	}

	public String getColumnValue(String columnFamilyName, String key,
			String columnName) {
		ColumnQuery<String, String, String> columnQuery = HFactory
				.createStringColumnQuery(keyspace);
		if (columnQuery == null)
			return null;

		columnQuery.setColumnFamily(columnFamilyName).setKey(key).setName(
				columnName);
		QueryResult<HColumn<String, String>> result = columnQuery.execute();
		if (result == null) {
			return null;
		}

		HColumn<String, String> columnNameValue = result.get();
		if (columnNameValue == null)
			return null;

		return columnNameValue.getValue();
	}

	@SuppressWarnings("unused")
	private void printStringColumnList(List<HColumn<String, String>> columns) {
		System.out.println("get data result size=" + columns.size());
		for (HColumn<String, String> data : columns) {
			System.out.println("column[" + data.getName() + "]="
					+ data.getValue());
		}
	}

	@SuppressWarnings("unused")
	private void printStringColumnList(HColumn<String, String> column) {
		System.out.println("column[" + column.getName() + "]="
				+ column.getValue());
	}

	@SuppressWarnings("unused")
	private void printUUIDColumnList(List<HColumn<UUID, String>> columns) {
		// print for test TODO rem the code
		System.out.println("get data result size=" + columns.size());
		for (HColumn<UUID, String> data : columns) {
			System.out.println("column[" + data.getName() + "]="
					+ data.getValue());
		}
	}

	@SuppressWarnings("unused")
	private void printStringRowList(List<Row<String, String, String>> rows) {
		for (Row<String, String, String> row : rows) {
			System.out.println("row key : " + row.getKey());
			ColumnSlice<String, String> columns = row.getColumnSlice();
			List<HColumn<String, String>> list = columns.getColumns();
			for (HColumn<String, String> data : list) {
				System.out.println("column[" + data.getName() + "]="
						+ data.getValue());
			}
		}
	}

	@SuppressWarnings("unused")
	private void printStringRow(Row<String, String, String> row) {

		System.out.println("row key : " + row.getKey());
		ColumnSlice<String, String> columns = row.getColumnSlice();
		List<HColumn<String, String>> list = columns.getColumns();
		for (HColumn<String, String> data : list) {
			System.out.println("column[" + data.getName() + "]="
					+ data.getValue());

		}
	}

	public List<HColumn<String, String>> getColumnKey(String columnFamilyName,
			String key, String... columnNames) {
		StringSerializer se = StringSerializer.get();
		SliceQuery<String, String, String> q = HFactory.createSliceQuery(
				keyspace, se, se, se);
		if (q == null) {
			return null;
		}

		q.setColumnFamily(columnFamilyName).setKey(key).setColumnNames(
				columnNames);

		QueryResult<ColumnSlice<String, String>> r = q.execute();
		if (r == null) {
			return null;
		}

		ColumnSlice<String, String> slices = r.get();
		if (slices == null) {
			return null;
		}

		List<HColumn<String, String>> result = slices.getColumns();

		return result;
	}

	public List<HColumn<String, String>> getColumnKey(String columnFamilyName,
			String key, int size) {
		SliceQuery<String, String, String> q = HFactory.createSliceQuery(
				keyspace, ss, ss, ss);
		if (q == null) {
			return null;
		}

		q.setColumnFamily(columnFamilyName).setKey(key).setRange(null, null,
				true, size);

		QueryResult<ColumnSlice<String, String>> r = q.execute();
		if (r == null) {
			return null;
		}

		List<HColumn<String, String>> result = r.get().getColumns();

		return result;
	}

	public List<HColumn<UUID, String>> getColumnKeyByRange(
			String columnFamilyName, String key, UUID start, int size) {
		return getColumnKeyByRange(columnFamilyName, key, start, null, size);
	}

	public List<HColumn<UUID, String>> getColumnKeyByRange(
			String columnFamilyName, String key, UUID start, UUID end, int size) {
		SliceQuery<String, UUID, String> q = HFactory.createSliceQuery(
				keyspace, ss, us, ss);
		if (q == null) {
			return null;
		}

		q.setColumnFamily(columnFamilyName).setKey(key).setRange(start, end,
				true, size);

		QueryResult<ColumnSlice<UUID, String>> r = q.execute();
		if (r == null) {
			return null;
		}

		List<HColumn<UUID, String>> result = r.get().getColumns();

		return result;
	}

    // use this method
	public List<HColumn<String, String>> getColumnKeyByStringRange(
			String columnFamilyName, String key, String start, String end,
			int size) {
		SliceQuery<String, String, String> q = HFactory.createSliceQuery(
				keyspace, ss, ss, ss);
		if (q == null) {
            log.warn(String.format("<cassandra> query(%s) key(%s) start(%s) end(%s) size(%d) but fail to create query",
                    columnFamilyName, key, start, end, size));
            return Collections.emptyList();
		}

		q.setColumnFamily(columnFamilyName).setKey(key).setRange(start, end,
				true, size);

		QueryResult<ColumnSlice<String, String>> r = q.execute();
		if (r == null || r.get() == null) {
            log.warn(String.format("<cassandra> query(%s) key(%s) start(%s) end(%s) size(%d) but no result",
                    columnFamilyName, key, start, end, size));
			return Collections.emptyList();
		}

		List<HColumn<String, String>> result = r.get().getColumns();
        log.warn(String.format("<cassandra> query(%s) key(%s) start(%s) end(%s) size(%d), %d result found",
                columnFamilyName, key, start, end, size, result.size()));
		return result;
	}

	public List<HColumn<String, String>> getAllColumns(String columnFamilyName,
			String key) {
		List<HColumn<String, String>> list = getColumnKeyByStringRange(
				columnFamilyName, key, null, null,
				UNLIMITED_COUNT);
		return list;
	}

	public Rows<String, String, String> getMultiRow(String columnFamilyName,
			String[] keys, String... columnNames) {
		MultigetSliceQuery<String, String, String> multigetSliceQuery = HFactory
				.createMultigetSliceQuery(keyspace, ss, ss, ss);
		multigetSliceQuery.setColumnFamily(columnFamilyName);
		multigetSliceQuery.setKeys(keys);
		multigetSliceQuery.setColumnNames(columnNames);
		QueryResult<Rows<String, String, String>> result = multigetSliceQuery
				.execute();
		Rows<String, String, String> rows = result.get();
		if (rows == null) {
			return null;
		}

		return rows;
	}

	public Rows<String, UUID, String> getMultiRowByRange(
			String columnFamilyName, String keyStart, String keyEnd,
			UUID start, UUID end, int maxCount) {
		RangeSlicesQuery<String, UUID, String> sliceQuery = HFactory
				.createRangeSlicesQuery(keyspace, ss, us, ss);
		sliceQuery.setColumnFamily(columnFamilyName);
		sliceQuery.setKeys(keyStart, keyEnd);
		sliceQuery.setRange(start, end, true, maxCount);
		// TODO:share this method with Benson.
		QueryResult<OrderedRows<String, UUID, String>> result = sliceQuery
				.execute();
		Rows<String, UUID, String> rows = result.get();
		if (rows == null) {
			return null;
		}
		return rows;
	}

	public Rows<String, String, String> getMultiRow(String columnFamilyName,
			String... keys) {
		MultigetSliceQuery<String, String, String> multigetSliceQuery = HFactory
				.createMultigetSliceQuery(keyspace, ss, ss, ss);
		multigetSliceQuery.setColumnFamily(columnFamilyName);
		multigetSliceQuery.setKeys(keys);
		multigetSliceQuery.setRange("", "", true, MAX_COUNT_FOR_MULTI_ROW);
		QueryResult<Rows<String, String, String>> result = multigetSliceQuery
				.execute();
		Rows<String, String, String> rows = result.get();
		if (rows == null) {
			return null;
		}

		return rows;
	}

	public Rows<String, String, String> getMultiRow(String columnFamilyName,
			int maxCount) {

		RangeSlicesQuery<String, String, String> q = HFactory
				.createRangeSlicesQuery(keyspace, ss, ss, ss);

		q.setColumnFamily(columnFamilyName);
		q.setKeys("", "");
		q.setRange("", "", true, maxCount);
		QueryResult<OrderedRows<String, String, String>> result = q.execute();
		OrderedRows<String, String, String> rows = result.get();
		if (rows == null) {
			return null;
		}

		return rows;
	}

	public int getColumnCount(String columnFamilyName, String key) {
		CountQuery<String, String> cq = HFactory.createCountQuery(keyspace, ss,
				ss);
		cq.setColumnFamily(columnFamilyName);
		cq.setKey(key);
		cq.setRange(null, null, UNLIMITED_COUNT);
		QueryResult<Integer> r = cq.execute();
		return r.get().intValue();
	}

	public int[] getMultiRowColumnCount(String columnFamilyName, String... keys) {
		int i = 0;
		int count[] = new int[keys.length];
		for (String key : keys) {
			count[i++] = getColumnCount(columnFamilyName, key);
		}
		return count;
	}

	public boolean deleteUUIDColumn(String columnFamilyName, String key,
			UUID uuid) {
		Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
		mutator.delete(key, columnFamilyName, uuid, us);
		mutator.execute();
		return true;
	}

    public boolean deleteStringColumn(String columnFamilyName, String key,
                                    String columnName) {
        Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
        mutator.delete(key, columnFamilyName, columnName, ss);
        mutator.execute();
        return true;
    }

	public boolean deleteMultipleColumns(String columnFamilyName, String key,
			String[] columnNames) {
		Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
		for (String queryString : columnNames) {
			mutator.addDeletion(key, columnFamilyName, queryString, ss);
		}
		mutator.execute();
		return true;
	}

	public boolean deleteMultipleColumns(String columnFamilyName, String key,
			UUID[] columnNames) {
		Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
		for (UUID queryString : columnNames) {
			mutator.addDeletion(key, columnFamilyName, queryString, us);
		}
		mutator.execute();
		return true;
	}

	public boolean deleteMultipleRows(String columnFamilyName, String[] rowNames) {
		Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
		for (String row : rowNames) {
			mutator.addDeletion(row, columnFamilyName, null, ss);
		}
		mutator.execute();
		return true;
	}

	public long increaseCounterColumn(String columnFamilyName, String rowKey,
			String columnName, long value) {
		String countStr = getColumnValue(columnFamilyName, rowKey, columnName);
		long count = 1;
		if (countStr != null && countStr.length() > 0)
			count = Long.parseLong(countStr) + 1;
		String columnValue = Long.toString(count);
		insert(columnFamilyName, rowKey, columnName, columnValue);
		return count;
	}
}
