package com.example.helloworld.util;

public class StartHelper {
	private static String configFile = null;

	public static String getConfigurationFile() {
		return configFile;
	}

	public static void setConfigurationFile(String configFile) {
		StartHelper.configFile = configFile;
	}
	
}
