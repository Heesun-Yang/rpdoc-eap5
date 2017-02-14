package com.rp.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.Random;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.ScatterRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

public class jfreeChartTest2 extends ApplicationFrame {

	public jfreeChartTest2(String s) {
        super(s);
        JPanel jpanel = createDemoPanel();
        jpanel.setPreferredSize(new Dimension(640, 480));
        add(jpanel);
    }

    public static JPanel createDemoPanel() {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
            "Scatter Plot Demo", "X", "Y", samplexydataset2(),
            PlotOrientation.VERTICAL, true, true, false);
        Shape cross = ShapeUtilities.createDiagonalCross(1, 1);
        
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesShape(0, cross);
        renderer.setSeriesPaint(0, Color.red);
        return new ChartPanel(jfreechart);
    }

    private static XYDataset samplexydataset2() {
    	XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		XYSeries series = new XYSeries("Random");
		/*
		int cols = 20;
		int rows = 20;
		double[][] values = new double[cols][rows];
        
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

    public static void main(String args[]) {
    	jfreeChartTest2 scatterplotdemo4 = new jfreeChartTest2("Scatter Plot Demo 4");
    	scatterplotdemo4.pack();
    	RefineryUtilities.centerFrameOnScreen(scatterplotdemo4);
    	scatterplotdemo4.setVisible(true);
    }
}
