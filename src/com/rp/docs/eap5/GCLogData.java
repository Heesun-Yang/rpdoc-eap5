package com.rp.docs.eap5;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCLogData {
	Logger log = LoggerFactory.getLogger(GCLogData.class);
	
	private ArrayList<MinorGcData> MinorGCLogList;
	private ArrayList<FullGcData> FullGCLogList;
	private ArrayList<CmsGcData> CmsGCLogList;
	
	public ArrayList<MinorGcData> getMinorGCLogList() {
		return MinorGCLogList;
	}
	public void setMinorGCLogList(ArrayList<MinorGcData> minorGCLogList) {
		MinorGCLogList = minorGCLogList;
	}
	public ArrayList<FullGcData> getFullGCLogList() {
		return FullGCLogList;
	}
	public void setFullGCLogList(ArrayList<FullGcData> fullGCLogList) {
		FullGCLogList = fullGCLogList;
	}
	public ArrayList<CmsGcData> getCmsGCLogList() {
		return CmsGCLogList;
	}
	public void setCmsGCLogList(ArrayList<CmsGcData> cmsGCLogList) {
		CmsGCLogList = cmsGCLogList;
	}
	
	// Get GC Interval
	public double getFullGCAvgInterval(){
		double FullGCAvgInterval = 0;
		
		// Avg GC Interval = LastFullGCTime / FullGcCount
		if (FullGCLogList.size() > 0){
			FullGcData fullGcData = FullGCLogList.get(FullGCLogList.size() - 1);
			FullGCAvgInterval = fullGcData.getTimeStamp() / FullGCLogList.size();
		} else {
			// Full GC가 없는 경우
			FullGCAvgInterval = -1;
		}
		
		
		
		return FullGCAvgInterval;
	}
	// get Max Gc Duration
	public double getMaxGcDuration(){
		double MaxFullGcTime = 0;
		
		// Full GC가 있으면
		if (FullGCLogList.size() > 0){
			
			for (FullGcData fullGcData : FullGCLogList){
				if (MaxFullGcTime < fullGcData.getFullGCTime()){
					MaxFullGcTime = fullGcData.getFullGCTime();
				}
				
			}
		}
		
		
		return MaxFullGcTime;
	}
}
