package net.angelspeech.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MessageResourcesManager {
	
	
	private static final String FILE_NAME = "/WEB-INF/classes/MessageResources.properties";
	
	private Properties properties;
	
	public MessageResourcesManager(String basedPath) {
		this.properties = new Properties();
		try {
			this.properties.load(new FileInputStream(basedPath + FILE_NAME));
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public String getValue(String key) {
		return this.properties.getProperty(key);
	}
	
//	public static String CONTEXT_PATH="";
//
//	private static final String delimiter = "\\+";
//	static Properties properties = null;
//	public static void initDefault() {
//		try {
//			File f = new File("MessageResources.properties");
//			if(f.exists())init(f);
//		} catch (Exception e) {
//		}
//	}
//
//	public Properties getProperties() {
//		return properties;
//	}
//
//	public static void init(File file) {
//		loadConfig(file);
//	}
//
//	private static void loadConfig(File file) {
//		/*
//		 * Load all the parameters from the resource file
//		 */
//		Properties tmpProperties = new Properties();
//
//		try {
//			tmpProperties.load(new FileInputStream(file));
//			tmpProperties.setProperty("CONTEXT_PATH", CONTEXT_PATH);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		/*
//		 * Transform the properties for "+" paths
//		 */
//
//		properties = new Properties();
//
//		for (Object key : tmpProperties.keySet()) {
//			String value = tmpProperties.getProperty(key.toString());
//			if (value != null && value.contains("+")) {
//				StringBuffer sb = new StringBuffer();
//				String[] keys = value.split(delimiter);
//				for (String k : keys) {
//					k = k.trim();
//					if (tmpProperties.containsKey(k)) {
//						sb.append(tmpProperties.getProperty(k));
//					} else {
//						sb.append(k);
//					}
//				}
//				value = sb.toString();
//			}
//			properties.put(key, value);
//		}
//	}
//
//	public static String getValue(String key) {
//		return properties.getProperty(key);
//	}
	
}
