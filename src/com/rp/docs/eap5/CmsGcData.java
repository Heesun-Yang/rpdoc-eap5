package com.rp.docs.eap5;

public class CmsGcData {
	private double TimeStamp;
	private double TotalUsedHeapMem;
	private double CmsGCTime;
	
	public double getTimeStamp() {
		return TimeStamp;
	}
	public void setTimeStamp(double timeStamp) {
		TimeStamp = timeStamp;
	}
	public double getTotalUsedHeapMem() {
		return TotalUsedHeapMem;
	}
	public void setTotalUsedHeapMem(double totalUsedHeapMem) {
		TotalUsedHeapMem = totalUsedHeapMem;
	}
	public double getCmsGCTime() {
		return CmsGCTime;
	}
	public void setCmsGCTime(double cmsGCTime) {
		CmsGCTime = cmsGCTime;
	}
	
}
