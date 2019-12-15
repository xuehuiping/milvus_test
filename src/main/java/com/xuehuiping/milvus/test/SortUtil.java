package com.xuehuiping.milvus.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SortUtil {
	static {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}

	public static List<Entry<String, Double>> sort1(Map<String, Double> map) {
		List<Entry<String, Double>> lists = new ArrayList<Entry<String, Double>>(map.entrySet());
		Collections.sort(lists, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				Double q1 = o1.getValue();
				Double q2 = o2.getValue();
				Double p = q2 - q1;
				if (p > 0) {
					return 1;
				} else if (p == 0) {
					return 0;
				} else
					return -1;
			}
		});
		return lists;
	}

	public static List<Entry<String, Integer>> sort2(Map<String, Integer> map) {
		List<Entry<String, Integer>> lists = new ArrayList<Entry<String, Integer>>(map.entrySet());
		Collections.sort(lists, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				int q1 = o1.getValue();
				int q2 = o2.getValue();
				int p = q2 - q1;
				if (p > 0) {
					return 1;
				} else if (p == 0) {
					return 0;
				} else
					return -1;
			}
		});
		return lists;
	}

	/**
	 * 同等权重合并
	 * 
	 * @param map1
	 * @param map2
	 * @return
	 * @author xuehp
	 * @date 2019年6月27日 下午12:35:10
	 */
	public static List<Entry<String, Double>> merge(Map<String, Double> map1, Map<String, Double> map2) {
		Map<String, Double> result = new HashMap<>();
		result.putAll(map1);
		Set<String> keys = map2.keySet();
		for (String key : keys) {
			Double value = map2.get(key);
			if (result.containsKey(key)) {
				result.put(key, value + result.get(key));
			} else {
				result.put(key, value);
			}
		}
		List<Entry<String, Double>> list = sort1(result);
		return list;
	}

	/**
	 * 加权累加
	 * 
	 * @param map1
	 *            第一个矩阵的得分
	 * @param weight1
	 *            第一个矩阵的权重
	 * @param map2
	 *            第二个矩阵的得分
	 * @param weight2
	 *            第二个矩阵的权重
	 * @return
	 * @author xuehp
	 * @date 2019年6月27日 下午12:41:29
	 */
	public static List<Entry<String, Double>> merge(Map<String, Double> map1, double weight1, Map<String, Double> map2,
			double weight2) {
		Map<String, Double> result = new HashMap<>();
		Set<String> keys1 = map1.keySet();
		for (String key : keys1) {
			Double value = map1.get(key) * weight1;
			result.put(key, value);
		}
		Set<String> keys2 = map2.keySet();
		for (String key : keys2) {
			Double value = map2.get(key) * weight2;
			if (result.containsKey(key)) {
				result.put(key, value + result.get(key));
			} else {
				result.put(key, value);
			}
		}
		List<Entry<String, Double>> list = sort1(result);
		return list;
	}

	/**
	 * 将value部分累加，权重均为1.0<BR>
	 * 其实不应该这么做。不同维度的数据，没有直接累加的合理性<br>
	 * 就好像身高+年龄<br>
	 * 
	 * @param map
	 * @return
	 * @author xuehp
	 * @date 2019年6月28日 下午12:04:29
	 */
	public static Double sum(Map<String, Double> map) {
		Set<String> keys = map.keySet();
		Double score = 0.0;
		for (String key : keys) {
			Double value = map.get(key);
			score += 1.0 * value;
		}
		return score;
	}

	public static Double sum2(Map<String, Integer> map) {
		Set<String> keys = map.keySet();
		Double score = 0.0;
		for (String key : keys) {
			Integer value = map.get(key);
			score += 1.0 * value;
		}
		return score;
	}
}
