package com.example.freq;
/**
 * @function 
 * @project zceam_final
 * @package com.bjzc.arithmetic
 * @filename TakeModular.java
 * @author dsb
 * @time   Jan 12, 2008
 * @version 1.2
 */
public class TakeModular {
	public static double[] getModularValue(Complex[] cx){
		double[] takeValue = new double[cx.length] ;
		for (int i = 0; i < cx.length; i++) {
			double real = cx[i].getReal() ;
			double image = cx[i].getimag() ;
			double temp = Math.sqrt(real*real + image*image) ;
			takeValue[i] = temp ;
		}
		return takeValue ;
	}
}
