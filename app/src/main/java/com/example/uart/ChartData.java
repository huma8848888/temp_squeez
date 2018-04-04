package com.example.uart;

import java.util.StringTokenizer;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChartData {
	Context mcontext;
	public ChartData(Context context){
		mcontext=context;
	}
	
/*	public  void LayoutDialog(View ch,double[] xelement){
		  buildchart(ch,R.id.linearmian);
		  updateChartUI((float)xelement[1]);
		  //考虑每次更新数据
	 }*/
	
	// 折线 
	private  XYSeries series;                     
	private  XYMultipleSeriesDataset mDataset;  //
    private  GraphicalView chart;
    private  XYMultipleSeriesRenderer renderer;
    private  int addX = -1;
    private  float addY;
    //只记录当前界面显示的图的点 
     int[] xx = new int[50];
     int[] yy = new int[50];
     
    public  void buildchart(View context,int chartid){
		LinearLayout layout = (LinearLayout)context.findViewById(chartid); 
        //layout.setBackgroundResource(R.drawable.bluestyle);
        series = new XYSeries("温度扫描趋势图");
        //创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();  
        //将点集添加到这个数据集中
        mDataset.addSeries(series);
        //renderer - 渲染器
        renderer = buildRenderer(Color.BLUE, PointStyle.CIRCLE );
        //设置图标样式
        setChartSettings(renderer, "X", "Y", 0, 50, -0.3, 0.3, Color.BLACK, Color.BLACK);
        //生成图表
        chart = ChartFactory.getLineChartView(context.getContext(), mDataset, renderer);
        //将图表添加到布局中去
		layout.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    public   void updateChartUI(float data,float min,float max) { 
    	addX = 0;
    	try{
    		addY =data;// (int)(Math.random() * 90);
        } catch (NumberFormatException err) {
        	return;  
        }
    	renderer.setYAxisMax(max);
    	renderer.setYAxisMin(min);
		//移除数据集中旧的点集
		mDataset.removeSeries(series);			
		//判断当前点集中到底有多少点
		int length = series.getItemCount();			
		if(length > 50){
			length = 50;
		}
		//  X Y轴
		for (int i = 0; i < length; i++) {
			//  X坐标每次的增量 + 5
			xx[i] = (int) series.getX(i) + 5;
			yy[i] = (int) series.getY(i);
		}	
		series.clear();
		//新出来的点肯定首先画，加到第一个
		series.add(addX, addY);
		//原来的点按照顺序加入
		for (int k = 0; k < length; k++) {
    		series.add(xx[k], yy[k]);
    	}
		mDataset.addSeries(series);
		//视图更新,如果在非UI主线程中，需要调用postInvalidate()
		chart.postInvalidate();
		//chart.invalidate();
    }
    public   void updateChartUI(double[] data) { 
		//移除数据集中旧的点集
		mDataset.removeSeries(series);			
		double maxValue = data[0];
		double minValue = data[0];
		series.clear();
		for (int k = 0; k < data.length; k++) {//k < 650
    		series.add(k, data[k]);
			if (maxValue < data[k]){maxValue = data[k];}
			if (minValue > data[k]){minValue = data[k];}
    	}
		mDataset.addSeries(series);
    	renderer.setYAxisMax(maxValue);
    	renderer.setYAxisMin(minValue);
    	renderer.setXAxisMin(0);
    	renderer.setXAxisMax(data.length);//650
		//视图更新,如果在非UI主线程中，需要调用postInvalidate()
		//chart.postInvalidate();
		chart.invalidate();
    }
    public   void updateChartUI(double[] ydata,double[] xdata,int ln) { 
		//移除数据集中旧的点集
		mDataset.removeSeries(series);			
		double maxValue = ydata[0];
		double minValue = ydata[0];
		series.clear();
		for (int k = 0; k < ln; k++) {//k < 650
    		series.add(xdata[k], ydata[k]);
			if (maxValue < ydata[k]){maxValue = ydata[k];}
			if (minValue > ydata[k]){minValue = ydata[k];}
    	}
		mDataset.addSeries(series);
    	renderer.setYAxisMax(maxValue);
    	renderer.setYAxisMin(minValue);
    	renderer.setXAxisMin(0);
    	renderer.setXAxisMax(ln);//650
		//视图更新,如果在非UI主线程中，需要调用postInvalidate()
		//chart.postInvalidate();
		chart.invalidate();
    }
	 /**
	   * 下面的2个方法直接从 AbstractDemoChart 中拷出来就好了。
	   * @param colors
	   * @param styles
	   * @return
	   */
    //设置渲染器
    protected  XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style){
    	XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
    	//设置图表中折线的样式
    	XYSeriesRenderer r = new XYSeriesRenderer();
    	r.setColor(color);   //线条颜色
    	r.setPointStyle(style); //点样式
    	r.setLineWidth(1); //线宽
    	renderer.addSeriesRenderer(r); //添加
    	return renderer;
    }	    
    // 设置图表的显示
    protected  void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
    								double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {

    	renderer.setChartTitle("");
    	renderer.setXTitle(xTitle);
    	renderer.setYTitle(yTitle);
    	renderer.setXAxisMin(xMin);
    	renderer.setXAxisMax(xMax);
    	renderer.setYAxisMin(yMin);
    	renderer.setYAxisMax(yMax);
    	renderer.setAxesColor(axesColor);
    	renderer.setLabelsColor(labelsColor);  
    	renderer.setShowGrid(true);       //是否显示网格
    	renderer.setGridColor(Color.RED); //网格的颜色
    	renderer.setXLabels(18);
    	renderer.setYLabels(10);
    	renderer.setXTitle("");            //设置title
    	renderer.setYTitle("");
    	renderer.setYLabelsAlign(Align.RIGHT); //Y周文字对齐方式
    	renderer.setPointSize((float)2); 
    	renderer.setYLabelsColor(0, Color.parseColor("#000000"));
    	renderer.setXLabelsColor(Color.parseColor("#000000"));
      	renderer.setShowLegend(false);
        //renderer.setLegendTextSize(15);
        //renderer.setLabelsTextSize(15);
      	
    	renderer.setBackgroundColor(Color.parseColor("#00000000"));
    	renderer.setMarginsColor(Color.argb(0, 0xF3, 0xF3, 0xF3)); //图表与周围四周的颜色
    	renderer.setMargins(new int[] {10, 20, 0, 10 }); //设置图表的边距
    }      
}
