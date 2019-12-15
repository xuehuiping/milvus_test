package com.xuehuiping.milvus.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 语义向量
 * 
 * @author xuehuiping
 * @date 2019年11月6日 下午6:16:57
 */
public class VectorUtil {

	/**
	 * 从word2vec文件加载语义向量
	 * 
	 * @param fileName
	 * @return void
	 * @author xuehp
	 * @throws IOException
	 * @date 2019年11月6日 下午6:18:10
	 */
	public static List<List<Float>> loadFromFile(String fileName) {
		List<List<Float>> vectors = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		try {
			lines = FileUtil.readLines2(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// 去除首行
		lines = lines.subList(1, lines.size() - 1);
		for (String line : lines) {
			String[] tmp = line.trim().split(" ");
			List<Float> vector = new ArrayList<>();
			// 跳过第一个
			for (int i = 1; i < tmp.length; i++) {
				String t = tmp[i].trim();
				Float e = Float.parseFloat(t);
				vector.add(e);
			}
			vectors.add(vector);
		}
		return vectors;
	}

	public static Map<String, List<Float>> loadToMap(String fileName) {
		Map<String, List<Float>> map = new HashMap<>();
		List<String> lines = new ArrayList<>();
		try {
			lines = FileUtil.readLines2(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// 去除首行
		lines = lines.subList(1, lines.size() - 1);
		for (String line : lines) {
			String[] tmp = line.trim().split(" ");
			List<Float> vector = new ArrayList<>();
			String term = tmp[0];
			for (int i = 1; i < tmp.length; i++) {
				String t = tmp[i].trim();
				Float e = Float.parseFloat(t);
				vector.add(e);
			}
			map.put(term, vector);
		}
		return map;
	}

	public static List<Entry<String, List<Float>>> loadToList(String fileName) {
		List<Entry<String, List<Float>>> list = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		try {
			lines = FileUtil.readLines2(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// 去除首行
		lines = lines.subList(1, lines.size() - 1);
		for (String line : lines) {
			String[] tmp = line.trim().split(" ");
			List<Float> vector = new ArrayList<>();
			String term = tmp[0];
			for (int i = 1; i < tmp.length; i++) {
				String t = tmp[i].trim();
				Float e = Float.parseFloat(t);
				vector.add(e);
			}
			String key = term;
			List<Float> value = vector;
			Entry<String, List<Float>> e = new MyEntry<String, List<Float>>(key, value);
			list.add(e);
		}
		return list;
	}

	/**
	 * 解析向量，便于建立向量索引
	 * 
	 * @param listWordVector
	 * @return
	 * @return List<List<Float>>
	 * @author xuehp
	 * @date 2019年11月6日 下午7:40:28
	 */
	public static List<List<Float>> getVectorList(List<Entry<String, List<Float>>> listWordVector) {
		List<List<Float>> vectors = new ArrayList<>();
		for (int i = 0; i < listWordVector.size(); i++) {
			Entry<String, List<Float>> entry = listWordVector.get(i);
			List<Float> e = entry.getValue();
			vectors.add(e);
		}
		return vectors;
	}

	/**
	 * 解析terms，方便构建矩阵
	 * 
	 * @param listWordVector
	 * @return
	 * @return List<String>
	 * @author xuehp
	 * @date 2019年11月6日 下午7:40:53
	 */
	public static List<String> getTerms(List<Entry<String, List<Float>>> listWordVector) {
		List<String> terms = new ArrayList<>();
		for (int i = 0; i < listWordVector.size(); i++) {
			Entry<String, List<Float>> entry = listWordVector.get(i);
			String e = entry.getKey();
			terms.add(e);
		}
		return terms;
	}

	public static Map<String, List<Float>> getMapTerm2Vector(List<Entry<String, List<Float>>> listWordVector) {
		Map<String, List<Float>> map = new HashMap<>();
		for (int i = 0; i < listWordVector.size(); i++) {
			Entry<String, List<Float>> entry = listWordVector.get(i);
			String key = entry.getKey();
			List<Float> value = entry.getValue();
			map.put(key, value);
		}
		return map;
	}

}
