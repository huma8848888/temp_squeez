package com.example.freq;

import com.example.uart.ChartData;


public class DataBeCounted {
	private ProcessData p = new ProcessData() ;
	public int type = 1 ;
	
	public void setType(int type){
		this.type = type;
	}

	/**
	 * 频谱图fft计算
	 * @param data
	 * @return
	 */
	public String getFFTdata(double[] data,double[] fftdata,double freq){
		StringBuffer sb = new StringBuffer() ;
		if (data != null) {
			//拿到y轴的值
			//double[] real = data ;
			//原始数据取半
			int size = 1024 ;
			Complex[] c = new Complex[size];
			double[] temp = new double[size];
			for (int i = 0; i < size; i++) {
				c[i] = new Complex();
				c[i].setReal(data[i]);
				c[i].setimag(0);
			}
			p.mwindow(temp, type);
			p.window(c, temp);
			p.msplfft(c, -1);
			double[]   yValue = TakeModular.getModularValue(c);
			//拿到x轴的值
			double[] xValues = new double[size];
			double fs = freq;//p.getFs();/////////////////////////////姝ゅ浼犳暟鎹簱閲岀殑棰戠巼
			for (int i = 0; i < size; i++) {
				xValues[i] = (fs/size )*(i) ;
			}
			for (int i = 0; i < size/2; i++) {
				fftdata[i]=yValue[i];
				//sb.append("(").append(xValues[i]).append(",").append(yValue[i]).append(");");
			}
			//InitLogger.writeLog.info("计算结果为：" + sb.toString());
			//InitLogger.writeLog.info("数据个数"+size+"取半"+size/2+" !");
			//InitLogger.writeLog.info("画频谱图所需的数据计算结束，匹配完成！");
		}
		return sb.toString();
	}
	
	public void getFFTdata(double[] data,ChartData chart,double freq){
		if (data != null) {
			//拿到y轴的值
			//double[] real = data ;
			//原始数据取半
			int size = data.length ;
			Complex[] c = new Complex[size];
			double[] temp = new double[size];
			for (int i = 0; i < size; i++) {
				c[i] = new Complex();
				c[i].setReal(data[i]);
				c[i].setimag(0);
			}
			p.mwindow(temp, type);
			p.window(c, temp);
			p.msplfft(c, -1);
			double[]   yValue = TakeModular.getModularValue(c);
			//拿到x轴的值
			double[] xValues = new double[size];
			double fs = freq;
			for (int i = 0; i < size/2; i++) {
				xValues[i] = (fs/size)*(i) ;
			}
			chart.updateChartUI(yValue, xValues,size/2);
			//InitLogger.writeLog.info("计算结果为：" + sb.toString());
			//InitLogger.writeLog.info("数据个数"+size+"取半"+size/2+" !");
			//InitLogger.writeLog.info("画频谱图所需的数据计算结束，匹配完成！");
		}
	}
}
