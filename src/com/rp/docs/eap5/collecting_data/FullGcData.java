package com.rp.docs.eap5.collecting_data;

public class FullGcData {
	private double TimeStamp;
	private double AfterFullGCTotalUsedHeapMem;
	private double AfterFullGCTotalUsedPermMem;
	private double FullGCTime;
	
	public double getTimeStamp() {
		return TimeStamp;
	}
	public void setTimeStamp(double timeStamp) {
		TimeStamp = timeStamp;
	}
	public double getAfterFullGCTotalUsedHeapMem() {
		return AfterFullGCTotalUsedHeapMem;
	}
	public void setAfterFullGCTotalUsedHeapMem(double afterFullGCTotalUsedHeapMem) {
		AfterFullGCTotalUsedHeapMem = afterFullGCTotalUsedHeapMem;
	}
	public double getAfterFullGCTotalUsedPermMem() {
		return AfterFullGCTotalUsedPermMem;
	}
	public void setAfterFullGCTotalUsedPermMem(double afterFullGCTotalUsedPermMem) {
		AfterFullGCTotalUsedPermMem = afterFullGCTotalUsedPermMem;
	}
	public double getFullGCTime() {
		return FullGCTime;
	}
	public void setFullGCTime(double fullGCTime) {
		FullGCTime = fullGCTime;
	}
	
	
}
