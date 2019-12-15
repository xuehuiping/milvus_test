package com.xuehuiping.milvus.test;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LogUtil {

	public static Logger getLog(Class<?> class1) {
		Logger log = LogManager.getLogger(class1.getClass());
		return log;
	}

}
