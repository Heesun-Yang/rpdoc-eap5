package com.rp.docs.eap5.collecting_data;

public class MinorGcData {
	private double TimeStamp;
	private double AfterMinorGCTotalUsedHeapMem;
	private double MinorGCTime;
	
	public double getTimeStamp() {
		return TimeStamp;
	}
	public void setTimeStamp(double timeStamp) {
		TimeStamp = timeStamp;
	}
	public double getAfterMinorGCTotalUsedHeapMem() {
		return AfterMinorGCTotalUsedHeapMem;
	}
	public void setAfterMinorGCTotalUsedHeapMem(double afterMinorGCTotalUsedHeapMem) {
		AfterMinorGCTotalUsedHeapMem = afterMinorGCTotalUsedHeapMem;
	}
	public double getMinorGCTime() {
		return MinorGCTime;
	}
	public void setMinorGCTime(double minorGCTime) {
		MinorGCTime = minorGCTime;
	}

	
}
