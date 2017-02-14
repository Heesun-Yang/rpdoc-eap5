package com.rp.docs.eap5;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawChart  extends ApplicationFrame {
	static Logger log = LoggerFactory.getLogger(ReportUtil.class);
	
	private XYSeriesCollection xySeriesCollection;
	private String PanelTitle;
	private GCLogData gcLogData;
	
	private int AxisInterval;
	
	public DrawChart(String s) {
        super(s);
    }
	
	// HeapMemory
	public JPanel createPanelTimestampHeapMemory() {
		return new ChartPanel(getChartTimestampHeapMemory());
    }
	
	// GCDuration
	public JPanel createPanelTimestampGCDuration() {
	   return new ChartPanel(getChartTimestampGCDuration());
    }
	
	// chart 그리기 (Timestamp - HeapMemory)
	public JFreeChart getChartTimestampHeapMemory() {
		JFreeChart jfreechart = ChartFactory.createScatterPlot(
				PanelTitle, "Timestamp (Sec)", "Heap Usage After GC (MB)", getXySeriesCollection(),
		    PlotOrientation.VERTICAL, true, true, false);
		
		// Point 크기
		Shape cross = ShapeUtilities.createDiagonalCross(1, 1);
		
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		
		 // X축 눈금 간격지정
		NumberAxis xAxis = (NumberAxis) xyPlot.getDomainAxis();
		//xAxis.setTickUnit(new NumberTickUnit(getAxisInterval()));
		NumberFormat formatterX = DecimalFormat.getInstance();
		formatterX.setMinimumFractionDigits(0);
		xAxis.setNumberFormatOverride(formatterX);
		
		// Y축 눈금
		NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
		//range.setRange(0.0, 1.0);
		//range.setTickUnit(new NumberTickUnit(10));
		NumberFormat formatterY = DecimalFormat.getInstance();
		formatterY.setMinimumFractionDigits(0);
		range.setNumberFormatOverride(formatterY);
		
		xyPlot.setDomainCrosshairVisible(true);
		xyPlot.setRangeCrosshairVisible(true);
		
		 // 배경색
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.black);
		xyPlot.setRangeGridlinePaint(Color.black);
		
		//                                                   		xyPlot.set
		XYItemRenderer renderer = xyPlot.getRenderer();
		
		// minor GC 
		renderer.setSeriesShape(0, cross);
		renderer.setSeriesPaint(0, new Color(030,144,255));  // Deep Sky blue
		
		// Full GC
		renderer.setSeriesPaint(1, Color.red);
        
		// CMS GC
       if (xySeriesCollection.getSeriesCount() == 3){
    	   renderer.setSeriesPaint(2, new Color(050,205,050));  // lime green
        }
       log.info("xySeriesCollection.getSeriesCount():{}", xySeriesCollection.getSeriesCount());
        
       return jfreechart;
    }

	// chart 그리기 (Timestamp - HeapMemory)
	public JFreeChart getChartTimestampGCDuration() {
		JFreeChart jfreechart = ChartFactory.createScatterPlot(
				PanelTitle, "Timestamp (Sec)", "Duration (Stop the World) (Sec)", getXySeriesCollection(),
		    PlotOrientation.VERTICAL, true, true, false);
		
		// Point 크기
		Shape shape0 = ShapeUtilities.createRegularCross(1, 2);
		Shape shape1 = ShapeUtilities.createDiamond(2);
		Shape shape2 = ShapeUtilities.createUpTriangle(2);
		
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		
		 // X축 눈금 간격지정
		NumberAxis xAxis = (NumberAxis) xyPlot.getDomainAxis();
		//xAxis.setTickUnit(new NumberTickUnit(getAxisInterval()));
		NumberFormat formatterX = DecimalFormat.getInstance();
		formatterX.setMinimumFractionDigits(0);
		xAxis.setNumberFormatOverride(formatterX);
		
		// Y축 눈금
		NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
		//range.setRange(0.0, 1.0);
		//range.setTickUnit(new NumberTickUnit(10));
		NumberFormat formatterY = DecimalFormat.getInstance();
		formatterY.setMinimumFractionDigits(0);
		range.setNumberFormatOverride(formatterY);
		
		xyPlot.setDomainCrosshairVisible(true);
		xyPlot.setRangeCrosshairVisible(true);
		
		 // 배경색
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.black);
		xyPlot.setRangeGridlinePaint(Color.black);
		
		//                                                   		xyPlot.set
		XYItemRenderer renderer = xyPlot.getRenderer();
		
		// minor GC 
		renderer.setSeriesShape(0, shape0);
		renderer.setSeriesPaint(0, new Color(030,144,255));  // Deep Sky blue
		
		// Full GC
		renderer.setSeriesShape(1, shape1);
		renderer.setSeriesPaint(1, Color.red);
        
		// CMS GC
       if (xySeriesCollection.getSeriesCount() == 3){
    	   renderer.setSeriesShape(2, shape2);
   			renderer.setSeriesPaint(2, new Color(050,205,050));  // lime green
        }
       
       
       
       log.info("xySeriesCollection.getSeriesCount():{}", xySeriesCollection.getSeriesCount());
        
       return jfreechart;
    }

    private static XYDataset samplexydataset2() {
    	XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		XYSeries series = new XYSeries("Random");
		
		int cols = 20;
		int rows = 20;
		double[][] values = new double[cols][rows];
        /*
		Random rand = new Random();
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
				double x = rand.nextGaussian();
				double y = rand.nextGaussian();
				series.add(x, y);
			}
		}*/
		series.add(1, 0.1);
		series.add(2, 0.5);
		series.add(3, 1);
		series.add(4, 1);
		series.add(5, 0.7);
		
       	
       xySeriesCollection.addSeries(series);
       return xySeriesCollection;
    }

    public XYSeriesCollection getXySeriesCollection() {
		return xySeriesCollection;
	}
    
    // Data 추가 Timestamp HeapMemory
    public void setXySeriesCollectionTimestampHeapMemory() {
		
		ArrayList<MinorGcData> MinorGCLogList = gcLogData.getMinorGCLogList();
		ArrayList<FullGcData> FullGCLogList = gcLogData.getFullGCLogList();
		ArrayList<CmsGcData> CmsGCLogList = gcLogData.getCmsGCLogList();
		
		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		XYSeries minorGcSeries = new XYSeries("Minor GC");
		XYSeries fullGcSeries = new XYSeries("Full GC");
		XYSeries cmsGcSeries = new XYSeries("CMS GC");
		
		double TimeStamp = 0;
		double AfterMinorGCTotalUsedHeapMem = 0;
		double TotalUsedHeapMem = 0;
		
		// UseParallelGC, UseParallelOldGC, UseConcMarkSweepGC, UseG1GC
		// Minor GC
		for (MinorGcData minorGcData : MinorGCLogList){
			
			TimeStamp = minorGcData.getTimeStamp();
			AfterMinorGCTotalUsedHeapMem = minorGcData.getAfterMinorGCTotalUsedHeapMem();
			minorGcSeries.add(TimeStamp, AfterMinorGCTotalUsedHeapMem);
			
		}
		seriesCollection.addSeries(minorGcSeries);
		
		// Full GC
		for (FullGcData fullGcData : FullGCLogList){
			
			TimeStamp = fullGcData.getTimeStamp();
			AfterMinorGCTotalUsedHeapMem = fullGcData.getAfterFullGCTotalUsedHeapMem();
			fullGcSeries.add(TimeStamp, AfterMinorGCTotalUsedHeapMem);
			
		}
		seriesCollection.addSeries(fullGcSeries);
		
		// UseConcMarkSweepGC 인 경우
		if ("UseConcMarkSweepGC".equals(ReportUtil.GCType)){
			// CMS GC
			for (CmsGcData cmsGcData : CmsGCLogList){
				
				TimeStamp = cmsGcData.getTimeStamp();
				TotalUsedHeapMem = cmsGcData.getTotalUsedHeapMem();
				cmsGcSeries.add(TimeStamp, TotalUsedHeapMem);
				
			}
			seriesCollection.addSeries(cmsGcSeries);
			
		}
		
		this.xySeriesCollection = seriesCollection;
		
	}
	
    // Data 추가 Timestamp GCDuration
    public void setXySeriesCollectionTimestampGCDuration() {
		
		ArrayList<MinorGcData> MinorGCLogList = gcLogData.getMinorGCLogList();
		ArrayList<FullGcData> FullGCLogList = gcLogData.getFullGCLogList();
		ArrayList<CmsGcData> CmsGCLogList = gcLogData.getCmsGCLogList();
		
		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		XYSeries minorGcSeries = new XYSeries("Minor GC");
		XYSeries fullGcSeries = new XYSeries("Full GC");
		XYSeries cmsGcSeries = new XYSeries("CMS GC");
		
		double TimeStamp = 0;
		double MinorGCTime = 0;
		double FullGCTime = 0;
		double CmsGCTime = 0;
		
		// UseParallelGC, UseParallelOldGC, UseConcMarkSweepGC, UseG1GC
		// Minor GC
		for (MinorGcData minorGcData : MinorGCLogList){
			
			TimeStamp = minorGcData.getTimeStamp();
			MinorGCTime = minorGcData.getMinorGCTime();
			minorGcSeries.add(TimeStamp, MinorGCTime);
			
		}
		seriesCollection.addSeries(minorGcSeries);
		
		// Full GC
		for (FullGcData fullGcData : FullGCLogList){
			
			TimeStamp = fullGcData.getTimeStamp();
			FullGCTime = fullGcData.getFullGCTime();
			fullGcSeries.add(TimeStamp, FullGCTime);
			
		}
		seriesCollection.addSeries(fullGcSeries);
		
		// UseConcMarkSweepGC 인 경우
		if ("UseConcMarkSweepGC".equals(ReportUtil.GCType)){
			// CMS GC
			for (CmsGcData cmsGcData : CmsGCLogList){
				
				TimeStamp = cmsGcData.getTimeStamp();
				CmsGCTime = cmsGcData.getCmsGCTime();
				cmsGcSeries.add(TimeStamp, CmsGCTime);
				
			}
			seriesCollection.addSeries(cmsGcSeries);
		}
		
		
		
		this.xySeriesCollection = seriesCollection;
		
	}
	
	
	public String getPanelTitle() {
		return PanelTitle;
	}
	public void setPanelTitle(String panelTitle) {
		PanelTitle = panelTitle;
	}
	
	
	public GCLogData getGcLogData() {
		return gcLogData;
	}

	public void setGcLogData(GCLogData gcLogData) {
		this.gcLogData = gcLogData;
	}

	public int getAxisInterval() {
		return AxisInterval;
	}
	public void setAxisInterval(int axisInterval) {
		AxisInterval = axisInterval;
	}
	public static void main(String args[]) {
    	DrawChart drawChart = new DrawChart("Scatter Plot Demo 4");
    	drawChart.setPanelTitle("Scatter Plot Demo 4");
    	
    	
    	File file = new File("data/services_result.json");
		byte[] JsonData;
		HashMap<String, Object> map = null;
		try {
			JsonData = FileUtils.readFileToByteArray(file);
			map = new ObjectMapper().readValue(JsonData, HashMap.class) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<Object> services = (ArrayList<Object>) map.get("services");
		//System.out.println("services.size():" + services.size());
		log.debug("===================================");
		log.debug("services count: " + services.size());
		log.debug("===================================");
		
		for (Object objService : services){
			LinkedHashMap<String, Object> service = (LinkedHashMap<String, Object>) objService;
			log.info("------------------------------------");
			log.info("service name: {}", service.get("name"));
			log.info("------------------------------------");
			
			// 서비스별 호스트 전체 데이터를 가지고 있는 변수
			ArrayList<Object> hosts = (ArrayList<Object>) service.get("jboss_hosts");
			
			for (Object objHost : hosts){
				LinkedHashMap<String, Object> host = (LinkedHashMap<String, Object>) objHost;
				ArrayList<Object> instances = (ArrayList<Object>) host.get("instances");
				log.info("  host: {}", host.get("ip"));
				
				for (Object objInstance : instances){
					
					LinkedHashMap<String, Object> instance = (LinkedHashMap<String, Object>) objInstance;
					log.info("    instance: {}", instance.get("instance_name"));
					//tableInstanceList.add(instance.get("instance_name").toString());
					
					HashMap<String, Object> instance_result_map = new HashMap<String, Object>();
					
					//결과 데이터 Map
					LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>)instance.get("result");
					
					
					//gc_log_inspection
					LinkedHashMap<String, Object> jboss_directory = (LinkedHashMap<String, Object>)result.get("jboss_directory");
					
					GCLogData gcLogData = null;
					String local_gc_log_file_path =  "data/gc.log";
					// GC Log파일 읽기
					LineIterator it = null;
					try {
						it = FileUtils.lineIterator(new File(local_gc_log_file_path));
						gcLogData = ReportUtil.getGCData(it);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
					    LineIterator.closeQuietly(it);
					}
					
					drawChart.setGcLogData(gcLogData);
					
					
					// GCDuration
					/*
					drawChart.setPanelTitle("GC Duration - " + instance.get("instance_name") + "");
					drawChart.setXySeriesCollectionTimestampGCDuration();
					JPanel jpanel = drawChart.createPanelTimestampGCDuration();
					*/
					
					// HeapMemory
					
					drawChart.setPanelTitle("Heap Usage After GC - " + instance.get("instance_name"));
					drawChart.setXySeriesCollectionTimestampHeapMemory();
					JPanel jpanel = drawChart.createPanelTimestampHeapMemory();
					
					jpanel.setPreferredSize(new Dimension(640, 480));
					drawChart.add(jpanel);
					
					drawChart.pack();
					RefineryUtilities.centerFrameOnScreen(drawChart);
					drawChart.setVisible(true);
					
				}
				
				// Hostname만 row에 담기
				//tableHostList.add(host.get("hostname").toString() + "(" + host.get("ip") + ")");
				//tableHostList.add(factory.createBr());
			}
			
		}
    	
	    
    }
}
