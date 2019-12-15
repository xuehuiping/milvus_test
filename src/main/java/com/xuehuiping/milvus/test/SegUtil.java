package com.xuehuiping.milvus.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.classification.utilities.TextProcessUtility;
import com.hankcs.hanlp.seg.common.Term;

public class SegUtil {

	private static final String SPLIT = " ";

	/**
	 * 提取关键词<br>
	 * 比例20%，最少5个
	 * 
	 * @author xuehp
	 * @date 2019年4月16日 下午2:50:36
	 * @param desc
	 * @return
	 */
	public static List<String> extractKeywords(String desc) {
		if (null == desc) {
			return new ArrayList<>();
		}
		desc = TextProcessUtility.preprocess(desc);
		int count = desc.length() / 5;
		count = Math.max(count, 5);
		List<String> list = HanLP.extractKeyword(desc, count);
		return list;
	}

	/**
	 * 分词<br>
	 * 自动预处理
	 * 
	 * @author xuehp
	 * @date 2019年4月16日 下午3:12:33
	 * @param text
	 * @return
	 */
	public static List<String> seg(String text) {
		String tmp = TextProcessUtility.preprocess(text);
		List<Term> list = HanLP.segment(tmp);
		List<String> result = new ArrayList<>();
		for (Term term : list) {
			if (!term.word.trim().isEmpty()) {
				result.add(term.word);
			}
		}
		return result;
	}

	/**
	 * 统计每个词的词频
	 *
	 * @param keywordArray
	 * @return
	 */
	public static List<Entry<String, Integer>> getKeywordCounts(List<String> keywordArray) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String word : keywordArray) {
			Integer counter = map.get(word);
			if (counter == null) {
				counter = 0;
			}
			map.put(word, ++counter); // 增加词频
		}
		return SortUtil.sort2(map);
	}

	/**
	 * 
	 * 分词<br>
	 * 自动预处理
	 * 
	 * @param text
	 *            待分词的文本
	 * @param SPLIT
	 *            分隔符
	 * @return String
	 * @author xuehp
	 * @date 2019年9月2日 下午5:17:23
	 */
	public static String seg(String text, String SPLIT) {
		if (null == text || text.isEmpty()) {
			return "";
		}
		String tmp = TextProcessUtility.preprocess(text);
		if (tmp.length() <= 3) {
			return tmp;
		}
		List<Term> list = HanLP.segment(tmp);
		String result = "";
		for (Term term : list) {
			if (!term.word.trim().isEmpty()) {
				result += term.word + SPLIT;
			}
		}
		result = result.trim();
		if (result.endsWith(SPLIT)) {
			result = result.substring(0, result.length() - SPLIT.length());
		}
		return result;
	}

	/**
	 * 给文件进行分词<br>
	 * 带有预处理，生成.seg文件
	 * 
	 * @param fileName
	 * @author xuehp
	 * @throws IOException
	 * @date 2019年09月23日19:36:17
	 */
	public static void segFile(String fileName) throws IOException {
		List<String> results = new ArrayList<>();
		List<String> lines = FileUtil.readLines2(fileName);
		for (String line : lines) {
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			String result = seg(line, SPLIT);
			results.add(result);
		}
		FileUtil.save(results, fileName + ".seg");
	}

	/**
	 * 中文，按照汉字字符分词
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public static void segFileByChar(String fileName) throws IOException {
		List<String> results = new ArrayList<>();
		List<String> lines = FileUtil.readLines2(fileName);
		for (String line : lines) {
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			String result = segByChar(line);
			results.add(result);
		}
		FileUtil.save(results, fileName + ".seg");
	}

	private static String segByChar(String text) {
		if (null == text || text.isEmpty()) {
			return "";
		}
		String tmp = TextProcessUtility.preprocess(text);
		String r = "";
		for (int i = 0; i < tmp.length(); i++) {
			char c = tmp.charAt(i);
			if (("" + c).trim().length() > 0) {
				r += c + SPLIT;
			}
		}
		return r.trim();
	}
}
