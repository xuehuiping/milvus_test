package com.xuehuiping.milvus.test;

import java.util.List;
import java.util.Map.Entry;

/**
 * 测试向量搜索引擎，提交文档和查询
 * @author xuehuiping
 * @date 2019-12-15 17:15:25
 */
public class MyClientTest {

	public static void main(String[] args) {
		//word2vec向量文件
		String fileName = "~/git/my_project/try_milvus/mymodel2.txt";
		List<Entry<String, List<Float>>> listordVector = VectorUtil.loadToList(fileName);
		String tableName = "kuakua_table";
		MyClient client = new MyClient(tableName, 100);
		client.insert(listordVector);
		client.query(listordVector, "学习好辛苦哦", 100);

	}

}
