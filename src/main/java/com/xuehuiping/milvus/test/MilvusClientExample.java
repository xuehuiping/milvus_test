package com.xuehuiping.milvus.test;


import io.milvus.client.*;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;


/**
 * https://github.com/milvus-io/milvus-sdk-java/blob/master/examples/src/main/java/MilvusClientExample.java
 * 
 * @author xuehuiping
 * @date 2019年11月6日 下午6:03:49
 */
public class MilvusClientExample {

  // Helper function that generates random vectors
  static List<List<Float>> generateVectors(long vectorCount, long dimension) {
    SplittableRandom splittableRandom = new SplittableRandom();
    List<List<Float>> vectors = new ArrayList<>();
    for (int i = 0; i < vectorCount; ++i) {
      splittableRandom = splittableRandom.split();
      DoubleStream doubleStream = splittableRandom.doubles(dimension);
      List<Float> vector =
          doubleStream.boxed().map(Double::floatValue).collect(Collectors.toList());
      vectors.add(vector);
    }
    return vectors;
  }

  // Helper function that normalizes a vector if you are using IP (Inner Product) as your metric
  // type
  static List<Float> normalizeVector(List<Float> vector) {
    float squareSum = vector.stream().map(x -> x * x).reduce((float) 0, Float::sum);
    final float norm = (float) Math.sqrt(squareSum);
    vector = vector.stream().map(x -> x / norm).collect(Collectors.toList());
    return vector;
  }

  public static void main(String[] args) throws InterruptedException, ConnectFailedException {

    // You may need to change the following to the host and port of your Milvus server
    final String host = "123.57.48.73";
    final String port = "19530";

    // Create Milvus client
    MilvusClient client = new MilvusGrpcClient();

    // Connect to Milvus server
    ConnectParam connectParam = new ConnectParam.Builder().withHost(host).withPort(port).build();
    try {
      Response connectResponse = client.connect(connectParam);
      System.out.println(connectResponse);
    } catch (ConnectFailedException e) {
      System.out.println(e.toString());
      throw e;
    }

    // Check whether we are connected
    boolean connected = client.isConnected();
    System.out.println("Connected = " + connected);

    // Create a table with the following table schema
    final String tableName = "example"; // table name
    final long dimension = 128; // dimension of each vector
    final long indexFileSize = 1024; // maximum size (in MB) of each index file
    final MetricType metricType = MetricType.IP; // we choose IP (Inner Product) as our metric type
    TableSchema tableSchema =
        new TableSchema.Builder(tableName, dimension)
            .withIndexFileSize(indexFileSize)
            .withMetricType(metricType)
            .build();
    Response createTableResponse = client.createTable(tableSchema);
    System.out.println(createTableResponse);

    // Check whether the table exists
    HasTableResponse hasTableResponse = client.hasTable(tableName);
    System.out.println(hasTableResponse);

    // Describe the table
    DescribeTableResponse describeTableResponse = client.describeTable(tableName);
    System.out.println(describeTableResponse);

    // Insert randomly generated vectors to table
    final int vectorCount = 100000;
    List<List<Float>> vectors = generateVectors(vectorCount, dimension);
    vectors =
        vectors.stream().map(MilvusClientExample::normalizeVector).collect(Collectors.toList());
    InsertParam insertParam = new InsertParam.Builder(tableName, vectors).build();
    InsertResponse insertResponse = client.insert(insertParam);
    System.out.println(insertResponse);
    // Insert returns a list of vector ids that you will be using (if you did not supply them
    // yourself) to reference the vectors you just inserted
    List<Long> vectorIds = insertResponse.getVectorIds();

    // The data we just inserted won't be serialized and written to meta until the next second
    // wait 1 second here
    TimeUnit.SECONDS.sleep(1);

    // Get current row count of table
    GetTableRowCountResponse getTableRowCountResponse = client.getTableRowCount(tableName);
    System.out.println(getTableRowCountResponse);

    // Create index for the table
    // We choose IVF_SQ8 as our index type here. Refer to IndexType javadoc for a
    // complete explanation of different index types
    final IndexType indexType = IndexType.IVF_SQ8;
    Index index = new Index.Builder().withIndexType(indexType).build();
    CreateIndexParam createIndexParam =
        new CreateIndexParam.Builder(tableName).withIndex(index).build();
    Response createIndexResponse = client.createIndex(createIndexParam);
    System.out.println(createIndexResponse);

    // Describe the index for your table
    DescribeIndexResponse describeIndexResponse = client.describeIndex(tableName);
    System.out.println(describeIndexResponse);

    // Search vectors
    // Searching the first 5 vectors of the vectors we just inserted
    final int searchBatchSize = 5;
    List<List<Float>> vectorsToSearch = vectors.subList(0, searchBatchSize);
    final long topK = 10;
    SearchParam searchParam =
        new SearchParam.Builder(tableName, vectorsToSearch).withTopK(topK).build();
    SearchResponse searchResponse = client.search(searchParam);
    System.out.println(searchResponse);
    if (searchResponse.getResponse().ok()) {
      List<List<SearchResponse.QueryResult>> queryResultsList =
          searchResponse.getQueryResultsList();
      final double epsilon = 0.001;
      for (int i = 0; i < searchBatchSize; i++) {
        // Since we are searching for vector that is already present in the table,
        // the first result vector should be itself and the distance (inner product) should be
        // very close to 1 (some precision is lost during the process)
        SearchResponse.QueryResult firstQueryResult = queryResultsList.get(i).get(0);
        if (firstQueryResult.getVectorId() != vectorIds.get(i)
            || Math.abs(1 - firstQueryResult.getDistance()) > epsilon) {
          throw new AssertionError("Wrong results!");
        }
      }
    }

    // Drop index for the table
    Response dropIndexResponse = client.dropIndex(tableName);
    System.out.println(dropIndexResponse);

    // Drop table
    Response dropTableResponse = client.dropTable(tableName);
    System.out.println(dropTableResponse);

    // Disconnect from Milvus server
    try {
      Response disconnectResponse = client.disconnect();
      System.out.println(disconnectResponse);
    } catch (InterruptedException e) {
      System.out.println("Failed to disconnect: " + e.toString());
      throw e;
    }
  }
}
