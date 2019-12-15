package com.xuehuiping.milvus.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.milvus.client.ConnectFailedException;
import io.milvus.client.ConnectParam;
import io.milvus.client.CreateIndexParam;
import io.milvus.client.HasTableResponse;
import io.milvus.client.Index;
import io.milvus.client.IndexType;
import io.milvus.client.InsertParam;
import io.milvus.client.InsertResponse;
import io.milvus.client.MetricType;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusGrpcClient;
import io.milvus.client.Response;
import io.milvus.client.SearchParam;
import io.milvus.client.SearchResponse;
import io.milvus.client.SearchResponse.QueryResult;
import io.milvus.client.TableSchema;

public class MyClient {
	private final String host = "123.57.219.113";
	private final String port = "19530";
	private MilvusClient client;
	private String tableName;
	private int dimension;
	private final long indexFileSize = 1024; // maximum size (in MB) of each
												// index file
	private final MetricType metricType = MetricType.IP; // we choose IP (Inner
															// Product)
	// as our metric type

	private Map<Long, String> mapVectorId2Term = new HashMap<>();
	private Map<String, List<Float>> mapTerm2Vector = new HashMap<>();
	private String mapVectorId2TermFileName = "data/mapVectorId2Term.txt";

	public MyClient(String tableName, int dimension) {
		client = new MilvusGrpcClient();

		ConnectParam connectParam = new ConnectParam.Builder().withHost(host).withPort(port).build();
		try {
			Response connectResponse = client.connect(connectParam);
			System.out.println(connectResponse);
		} catch (ConnectFailedException e) {
			System.out.println(e.toString());
		}
		this.dimension = dimension;
		this.tableName = tableName;
		System.out.println("使用表名：" + tableName);

	}

	public void query(List<Entry<String, List<Float>>> listordVector, String str, double dim) {
		System.out.println("查询串：" + str);
		// 这里需要把查询串分词，然后拼接list
		if (0 == mapTerm2Vector.size()) {
			mapTerm2Vector = VectorUtil.getMapTerm2Vector(listordVector);
		}
		List<List<Float>> vectors = new ArrayList<>();
		List<String> terms = SegUtil.seg(str);
		for (String term : terms) {
			List<Float> vector = mapTerm2Vector.get(term);
			if (null != vector) {
				vectors.add(vector);
			}
		}
		System.out.println(vectors.size() + "\t" + terms);
		if (0 == mapVectorId2Term.size()) {
			try {
				mapVectorId2Term = FileUtil.readToMap(mapVectorId2TermFileName);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("加载文件失败！fileName=" + mapVectorId2TermFileName);
				return;
			}
		}

		final int searchBatchSize = Math.min(5, vectors.size());
		List<List<Float>> vectorsToSearch = vectors.subList(0, searchBatchSize);
		final long topK = 10;
		SearchParam searchParam = new SearchParam.Builder(tableName, vectorsToSearch).withTopK(topK).build();
		SearchResponse searchResponse = client.search(searchParam);
		System.out.println(searchResponse);
		if (searchResponse.getResponse().ok()) {
			List<List<SearchResponse.QueryResult>> queryResultsList = searchResponse.getQueryResultsList();
			final double epsilon = 0.1;
			for (int i = 0; i < queryResultsList.size(); i++) {
				// Since we are searching for vector that is already present in
				// the table,
				// the first result vector should be itself and the distance
				// (inner product) should be
				// very close to 1 (some precision is lost during the process)
				List<QueryResult> results = queryResultsList.get(i);
				results = results.subList(1, results.size());// 去除自己
				for (QueryResult result : results) {
					if (Math.abs(result.getDistance() / dim) <= epsilon) {
						System.out.println(mapVectorId2Term.get(result.getVectorId()) + "\t" + result.getVectorId()
								+ "\t" + result.getDistance());
					}
				}
				System.out.println();
			}
		}

	}

	public void insert(List<Entry<String, List<Float>>> listWordVector) {
		checkTable();
		List<List<Float>> vectors = VectorUtil.getVectorList(listWordVector);
		vectors = vectors.stream().map(MilvusClientExample::normalizeVector).collect(Collectors.toList());
		InsertParam insertParam = new InsertParam.Builder(tableName, vectors).build();
		InsertResponse insertResponse = client.insert(insertParam);
		System.out.println(insertResponse);
		List<Long> vectorIds = insertResponse.getVectorIds();
		List<String> terms = VectorUtil.getTerms(listWordVector);
		int i = 0;
		for (Long vectorId : vectorIds) {
			Long key = vectorId;
			String value = terms.get(i);
			mapVectorId2Term.put(key, value);
			i++;
		}
		try {
			FileUtil.save2(mapVectorId2Term, mapVectorId2TermFileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		mapTerm2Vector = VectorUtil.getMapTerm2Vector(listWordVector);
		// The data we just inserted won't be serialized and written to meta
		// until the next second
		// wait 1 second here
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		final IndexType indexType = IndexType.IVF_SQ8;
		Index index = new Index.Builder().withIndexType(indexType).build();
		CreateIndexParam createIndexParam = new CreateIndexParam.Builder(tableName).withIndex(index).build();
		Response createIndexResponse = client.createIndex(createIndexParam);
		System.out.println(createIndexResponse);

	}

	private void checkTable() {
		HasTableResponse hasTableResponse = client.hasTable(tableName);
		System.out.println(hasTableResponse);
		if (hasTableResponse.hasTable()) {
			System.out.println("表已经存在，将删除");

			Response dropIndexResponse = client.dropIndex(tableName);
			System.out.println(dropIndexResponse);

			// Drop table
			Response dropTableResponse = client.dropTable(tableName);
			System.out.println(dropTableResponse);
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("创建表");

		TableSchema tableSchema = new TableSchema.Builder(tableName, dimension).withIndexFileSize(indexFileSize)
				.withMetricType(metricType).build();
		Response createTableResponse = client.createTable(tableSchema);
		System.out.println(createTableResponse);

		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}