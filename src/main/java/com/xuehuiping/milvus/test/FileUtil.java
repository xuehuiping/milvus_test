package com.xuehuiping.milvus.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;

public class FileUtil {
	static Logger log = LogUtil.getLog(FileUtil.class);

	/**
	 * 不读取空行
	 * 
	 * @param fileName
	 * @return
	 */
	public static List<String> readLinesWithoutBlank(String fileName) {
		List<String> lines = readLinesAll(fileName);
		List<String> result = new ArrayList<>();
		for (String line : lines) {
			line = line.trim();
			if (!line.isEmpty()) {
				result.add(line);
			}
		}
		return result;
	}

	public static List<String> readLinesAll(String fileName) {
		List<String> lines = new ArrayList<>();
		try {
			lines = Files.readAllLines(Paths.get(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static Set<String> readLinesToSet(String fileName) {
		Set<String> lines = new HashSet<>();
		try {
			List<String> tmp = Files.readAllLines(Paths.get(fileName));
			for (String line : tmp) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static void save(List<String> lines, String fileName) throws IOException {
		Files.write(Paths.get(fileName), lines);
	}

	public static void save(String content, String fileName) throws IOException {
		byte[] bytes = content.getBytes();
		Files.write(Paths.get(fileName), bytes);
	}

	public static String readFile(String fileName) throws IOException {
		String charsetName = "utf-8";
		return readFile(fileName, charsetName);
	}

	public static String readFile(String fileName, String charsetName) throws IOException {
		String content = "";
		Reader reader = new InputStreamReader(new FileInputStream(fileName), charsetName);
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while ((line = br.readLine()) != null) {
			content += line + "\r\n";
		}
		br.close();
		reader.close();
		return content;
	}

	public static boolean exist(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

	/**
	 * 读取文件，utf-8格式
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 *             return List<String>
	 * @author xuehp
	 * @date 2019年7月29日 下午7:27:05
	 */
	public static List<String> readLines2(String fileName) throws IOException {
		return readLines2(fileName, "UTF-8");
	}

	public static List<String> readLines2(String fileName, String charset) throws IOException {
		log.info("正在读取文件：" + fileName);
		File file = new File(fileName);
		if (!file.exists()) {
			log.error("file '" + fileName + "' not exist.");
			return null;
		}
		InputStream input = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
		List<String> list = new ArrayList<String>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() > 0) {
				list.add(line);
			}
		}
		reader.close();
		input.close();
		return list;
	}

	/**
	 * 列举文件夹下面的所有文件。返回文件名称的绝对路径
	 * 
	 * @param folder
	 * @return return List<String>
	 * @author xuehp
	 * @date 2019年7月25日 上午10:05:11
	 */
	public static List<String> listFolder(String folder) {
		List<String> list = new ArrayList<>();
		if (!folder.endsWith("/")) {
			folder += "/";
		}
		File file = new File(folder);
		if (!file.isDirectory()) {
			log.error("不是文件夹。folder=" + folder);
			return list;
		}
		String[] files = file.list();
		for (String f : files) {
			list.add(folder + f);
		}
		return list;
	}

	/**
	 * 获取文件的编码格式
	 * 
	 * @param fileName
	 *            return void
	 * @author xuehp
	 * @return
	 * @date 2019年7月25日 上午10:16:03
	 */
	public static String getEncoding(String fileName) {
		java.io.File f = new java.io.File(fileName);
		try {
			java.io.InputStream ios = new java.io.FileInputStream(f);
			byte[] b = new byte[3];
			ios.read(b);
			ios.close();
			if (b[0] == -17 && b[1] == -69 && b[2] == -65)
				return "utf-8";
			else {
				return "GBK";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void save(Set<String> lines, String fileName) throws IOException {
		Files.write(Paths.get(fileName), lines);
	}

	public static boolean exist(String folder, String fileName) {
		return exist(folder + fileName);
	}

	public static List<String> sub(String fileName1, String fileName2) throws IOException {
		List<String> list1 = readLines2(fileName1);
		List<String> list2 = readLines2(fileName2);
		boolean r = list1.removeAll(list2);
		log.info(r);
		return list1;
	}

	/**
	 * 将文件读入到map<br>
	 * value=整行
	 * 
	 * @param fileName
	 * @param keyIndex
	 * @return return Map<String,String>
	 * @author xuehp
	 * @throws IOException
	 * @date 2019年8月27日 下午5:05:25
	 */
	public static Map<String, String> readLinesToMap(String fileName, int keyIndex, String split) throws IOException {
		log.info("正在读取文件：" + fileName);
		Map<String, String> map = new HashMap<String, String>();
		File file = new File(fileName);
		if (!file.exists()) {
			log.error("file '" + fileName + "' not exist.");
			return null;
		}
		InputStream input = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() > 0) {
				String[] sets = line.split(split);
				String key = sets[keyIndex];
				String value = line;
				map.put(key, value);
			}
		}
		reader.close();
		input.close();
		return map;
	}

	/**
	 * 随机读取一行
	 * 
	 * @param fileName
	 * @return
	 * @author xuehp
	 * @throws IOException
	 * @date Sep 9, 2019 12:24:18 PM
	 */
	public static String readRandom(String fileName) throws IOException {
		List<String> lines = readLines2(fileName);
		Collections.shuffle(lines);
		return lines.get(0);
	}

	/**
	 * 随机读取一行
	 * 
	 * @param fileName
	 * @param charset
	 * @return
	 * @throws IOException
	 * @author xuehp
	 * @date Sep 9, 2019 1:00:35 PM
	 */
	public static String readRandom(String fileName, String charset) throws IOException {
		List<String> lines = readLines2(fileName, charset);
		Collections.shuffle(lines);
		return lines.get(0);
	}

	/**
	 * 按行读文件
	 * 
	 * @param String
	 *            fileName 待读取的文件
	 * 
	 * @return List<String> 读取的内容，按行存放在List中
	 */
	public static List<String> readResourceByLines(String fileName) throws IOException {
		InputStream input = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
		// set encoding utf-8 when read utf-8 file, because some time jvm is not
		// utf-8 encoding
		BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
		List<String> list = new ArrayList<String>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() > 0) {
				list.add(line);
			}
		}
		reader.close();
		input.close();
		return list;
	}

	/**
	 * 保存map<br>
	 * key，value 格式，制表符分隔<br>
	 * （在文件的末尾添加，不覆盖原文件的内容）
	 * 
	 * @param map
	 * @param fileName
	 * @author xuehp
	 * @throws IOException
	 * @date Sep 10, 2019 3:49:54 PM
	 */
	public static void save(Map<String, Integer> map, String fileName) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter writer = new FileWriter(fileName, true);
		Set<String> keys = map.keySet();
		for (String key : keys) {
			String line = key + "\t" + map.get(key);
			writer.write(line + "\r\n");
		}
		writer.close();
	}

	/**
	 * 将List<Sting>中的内容保存到文件（覆盖原文件的内容）
	 * 
	 * @param String
	 *            fileName 要保存到的文件的名称
	 * 
	 * @param List<String>
	 *            list 待保存的list
	 */
	public static void saveFile(String fileName, List<String> list) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter writer = new FileWriter(fileName);
		for (String line : list) {
			writer.write(line + "\r\n");
		}
		writer.close();
	}

	public static void save2(List<Entry<String, Integer>> list, String fileName) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter writer = new FileWriter(fileName);
		for (Entry<String, Integer> line : list) {
			writer.write(line + "\r\n");
		}
		writer.close();
	}

	/**
	 * 返回相对目录
	 * 
	 * @param folder
	 * @return
	 * @author xuehp
	 * @date Sep 11, 2019 9:50:59 PM
	 */
	public static List<String> listFolder2(String folder) {
		List<String> list = new ArrayList<>();
		if (!folder.endsWith("/")) {
			folder += "/";
		}
		File file = new File(folder);
		if (!file.isDirectory()) {
			log.error("不是文件夹。folder=" + folder);
			return list;
		}
		String[] files = file.list();
		for (String f : files) {
			if (".DS_Store".equals(f)) {
				continue;
			}
			list.add(f);
		}
		return list;
	}

	/**
	 * 创建目录
	 * 
	 * @param folderName
	 * @author xuehp
	 * @date Sep 12, 2019 9:42:10 AM
	 */
	public static void mkdir(String folderName) {
		File file = new File(folderName);
		file.mkdirs();
	}

	/**
	 * 将map写入文件,tab分隔
	 * 
	 * @param map
	 * @param fileName
	 * @throws IOException
	 * @return void
	 * @author xuehp
	 * @date 2019年11月6日 下午8:10:01
	 */
	public static void save2(Map<Long, String> map, String fileName) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter writer = new FileWriter(fileName, true);
		Set<Long> keys = map.keySet();
		for (Long key : keys) {
			String line = key + "\t" + map.get(key);
			writer.write(line + "\r\n");
		}
		writer.close();
	}

	/**
	 * 将文件读入map，tab分隔
	 * 
	 * @param fileName
	 * @return
	 * @return Map<Long,String>
	 * @author xuehp
	 * @throws IOException
	 * @date 2019年11月6日 下午8:10:16
	 */
	public static Map<Long, String> readToMap(String fileName) throws IOException {
		Map<Long, String> map = new HashMap<>();
		List<String> lines = readLines2(fileName);
		for (String line : lines) {
			String[] sets = line.split("\t");
			Long key = Long.parseLong(sets[0]);
			String value = sets[1];
			map.put(key, value);
		}
		return map;
	}

}
