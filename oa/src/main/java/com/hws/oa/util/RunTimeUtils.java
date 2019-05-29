package com.hws.oa.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class RunTimeUtils {

	private static final String WINDOWS="WINDOWS";
	/**
	 * 检查当前系统
	 */
	public static boolean isWindows(){
		String systemName = System.getProperty("os.name");
		if(systemName.toUpperCase().contains(WINDOWS)) return true;
		return false;
	}
	public static List<String> excute(String pomPath,String command) throws IOException, InterruptedException{
		Runtime runtime=Runtime.getRuntime();
		Process process=null; 
		if(isWindows()){
			process= runtime.exec("cmd /c   cd "+pomPath+" &&"+command);
			
		}else{
			process= runtime.exec("cd "+pomPath+" &&"+command);
		}
		String line = null;
		List<String> messages = new ArrayList<String>();
		InputStream input = process.getInputStream();
		if(null == input)return messages;
		BufferedReader read = new BufferedReader(new InputStreamReader(input,"GB2312"));
		while((line= read.readLine())!=null){
			messages.add(line);
		}
		process.waitFor();
	    process.destroy();
		return messages;
	}
}
