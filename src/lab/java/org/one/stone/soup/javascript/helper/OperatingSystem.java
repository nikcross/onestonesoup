package org.one.stone.soup.javascript.helper;

import java.io.File;
import java.lang.management.ManagementFactory;

public class OperatingSystem {

	public static String getOperatingSystem() {
		return System.getProperty("os.name");
	}
	
	public static String getOperatingSystemVersion() {
		return System.getProperty("os.version");
	}
	
	public static String getJavaVersion() {
		return System.getProperty("java.vendor")+" "+System.getProperty("java.version");
	}
	
	public static long getTime() {
		return System.currentTimeMillis();
	}
	
	public static long getStartTime() {
		return ManagementFactory.getRuntimeMXBean().getStartTime();
	}
	
	public static long getDriveTotalSpace(String path) {
		return new File(path).getTotalSpace();
	}
	
	public static long getDriveFreeSpace(String path) {
		return new File(path).getFreeSpace();
	}
	
	public static String getProcessor() {
		return System.getProperty("os.arch");
	}
	
	public static int getProcessorLoad() {
		return (int)(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()*100);
	}
	
	public static long getProcessorUsed() {
		return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}
	
	public static int getProcessors() {
		return ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	}
	
	public static long getMemory() {
		return Runtime.getRuntime().totalMemory();
	}
	
	public static long getMemoryUsed() {
		return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}
}
