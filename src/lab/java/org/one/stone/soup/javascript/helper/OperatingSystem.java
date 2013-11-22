package org.one.stone.soup.javascript.helper;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

public class OperatingSystem {

	public String getOperatingSystem() {
		return System.getProperty("os.name");
	}
	
	public String getOperatingSystemVersion() {
		return System.getProperty("os.version");
	}
	
	public String getJavaVersion() {
		return System.getProperty("java.vendor")+" "+System.getProperty("java.version");
	}
	
	public long getTime() {
		return System.currentTimeMillis();
	}
	
	public long getStartTime() {
		return ManagementFactory.getRuntimeMXBean().getStartTime();
	}
	
	public long getDriveTotalSpace(String path) {
		return new File(path).getTotalSpace();
	}
	
	public long getDriveFreeSpace(String path) {
		return new File(path).getFreeSpace();
	}
	
	public String getProcessor() {
		return System.getProperty("os.arch");
	}
	
	public int getProcessorLoad() {
		return (int)(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()*100);
	}
	
	public long getProcessorUsed() {
		return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}
	
	public int getProcessors() {
		return ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	}
	
	public long getMemory() {
		return Runtime.getRuntime().totalMemory();
	}
	
	public long getMemoryUsed() {
		return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}
}
