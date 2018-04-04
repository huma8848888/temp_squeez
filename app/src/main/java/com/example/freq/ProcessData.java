package com.example.freq;
public class ProcessData {
	
	//private double fs = JMDJconstant.FREQUENCY_SIZE ;
	
	
	/**
	 * 进行fft处理
	 * 
	 * @param x
	 * @param n
	 * @param isign
	 */
	public void msplfft(Complex[] x, int isign)
	{
	/*----------------------------------------------------------------------
	Routine msplfft:to perform the split-radix DIF fft algorithm.
	input parameters:
	x : complex array.input signal is stored in x(0) to x(n-1).
	n : the dimension of x.
	isign:if isign=-1 For Forward Transform
	if isign=+1 For Inverse Transform.
	output parameters:
	x : complex array. DFT result is stored in x(0) to x(n-1).
	Notes:
	n must be power of 2.
	in chapter 5
		----------------------------------------------------------------------*/
		Complex xt = new Complex();
		int n = x.length ;
		double es,e,a,a3,cc1,ss1,cc3,ss3,r1,r2,s1,s2,s3;
		int m,n2,k,n4,j,is,id,i1,i2,i3,i0,n1,i,nn;
		for(m=1;m<=16;m++)
		{
			nn=(int)Math.pow(2,m);
			if(n==nn)
				break;
		}
		if(m>16)
		{
	        System.out.println(" N is not a power of 2 ! \n");
			return;
		}
		n2=n*2;
		es=(double)(-isign*Math.atan(1.0)*8.0);
		for(k=1;k<m;k++)
		{
			n2=n2/2;
			n4=n2/4;
			e=es/n2;
			a=0.0;
			for(j=0;j<n4;j++)
			{
				a3=3*a;
				cc1=(double)Math.cos(a);
				ss1=(double)Math.sin(a);
				cc3=(double)Math.cos(a3);
				ss3=(double)Math.sin(a3);
				a=(j+1)*e;
				is=j;
				id=2*n2;
				do
				{
					for(i0=is;i0<n;i0+=id)
					{
						i1=i0+n4;
						i2=i1+n4;
						i3=i2+n4;
						r1=x[i0].getReal()-x[i2].getReal();
						s1=x[i0].getimag()-x[i2].getimag();
						r2=x[i1].getReal()-x[i3].getReal();
						s2=x[i1].getimag()-x[i3].getimag();
						
//						x[i0].getReal+=x[i2].getReal;
						x[i0].setReal(x[i0].getReal()+x[i2].getReal());
//						x[i0].imag+=x[i2].imag;
						x[i0].setimag(x[i0].getimag()+x[i2].getimag());
						//x[i1].getReal+=x[i3].getReal;
						x[i1].setReal(x[i1].getReal()+x[i3].getReal());
//						x[i1].imag+=x[i3].imag;
						x[i1].setimag(x[i1].getimag()+x[i3].getimag());
						if(isign!=1)
						{
							s3=r1-s2;
							r1+=s2;
							s2=r2-s1;
							r2+=s1;
						}
						else
						{
							s3=r1+s2;
							r1=r1-s2;
							s2=-r2-s1;
							r2=-r2+s1;
						}
						x[i2].setReal(r1*cc1-s2*ss1);
						x[i2].setimag(-s2*cc1-r1*ss1);
						x[i3].setReal(s3*cc3+r2*ss3);
						x[i3].setimag(r2*cc3-s3*ss3);
					}
					is=2*id-n2+j;
					id=4*id;
				}while(is<n-1);
			}
		}
		/*   ------------ special last stage -------------------------*/
		is=0;
		id=4;
		do
		{
	        for(i0=is;i0<n;i0+=id)
			{
				i1=i0+1;
//				xt.real=x[i0].real;
				xt.setReal(x[i0].getReal());
//				xt.imag=x[i0].imag;
				xt.setimag(x[i0].getimag());
//				x[i0].real=xt.real+x[i1].real;
				x[i0].setReal(xt.getReal()+x[i1].getReal());
//				x[i0].imag=xt.imag+x[i1].imag;
				x[i0].setimag(xt.getimag()+x[i1].getimag());
//				x[i1].real=xt.real-x[i1].real;
				x[i1].setReal(xt.getReal()-x[i1].getReal());
//				x[i1].imag=xt.imag-x[i1].imag;
				x[i1].setimag(xt.getimag()-x[i1].getimag());
			}
	        is=2*id-2;
	        id=4*id;
		}while(is<n-1);
		j=1;
		n1=n-1;
		for(i=1;i<=n1;i++)
		{
			if(i<j)
			{
//				xt.real=x[j-1].real;
				xt.setReal(x[j-1].getReal());
//				xt.imag=x[j-1].imag;
				xt.setimag(x[j-1].getimag());
//				x[j-1].real=x[i-1].real;
				x[j-1].setReal(x[i-1].getReal());
//				x[j-1].imag=x[i-1].imag;
				x[j-1].setimag(x[i-1].getimag());
//				x[i-1].real=xt.real;
				x[i-1].setReal(xt.getReal());
//				x[i-1].imag=xt.imag;
				x[i-1].setimag(xt.getimag());
			}
			k=n/2;
			do
			{
				if(k>=j)
					break;
				j-=k;
				k/=2;
			}while(true);
			j+=k;
		}
		//	if(isign==-1)
		
		for(i=0;i<n;i++)
		{
//			x[i].real/=(double)(n*0.5);
			x[i].setReal(x[i].getReal() / (n*0.5));
			
//			x[i].imag/=(double)(n*0.5);
			x[i].setimag(x[i].getimag() /(n*0.5));
		}
		return;
	}
	

	/**
	 * 获得窗体function
	 * 
	 * @param w
	 * @param n
	 */
	public void mwindow(double[] w,int wtype) {

		double PI = 3.141592653f;
		double pn = 0 ;
		int nCount = w.length ;
	
		 int i;
		 pn=(double)(2*PI/nCount);
		 
		 switch(wtype)
		 {
		 case 0:
			 for(i = 0; i < nCount; i++)
				 w[i] = 1.0;
			 break;
		 case 1:
			for(i = 0; i < nCount; i++)
				w[i] = 1.0-Math.cos(pn*i);
			break;
		 case 2:
			for(i = 0; i < nCount; i++)
				w[i] = 1.08-0.92*Math.cos(pn*i);
			break;
		 default:
			 break;
		 }

	}

	/**
	 * 加窗
	 * 
	 * @param src
	 * @param w
	 */
	public void window(Complex[] src, double[] w) {
		for (int i = 0; i < w.length; i++) {
			src[i].setReal(src[i].getReal() * w[i]);
		}
	}

	/**
	 * 得到相位�?
	 * 
	 * @param src
	 * @param location
	 * @return
	 */
	public double getPhase(Complex[] src, int location) {

		return Math.atan2((src[location].getimag()), (src[location].getReal()));
	}

	/**
	 * 模拟XXX输入信号
	 * 
	 * @param dest
	 * @param fs
	 * @param f0
	 * @param delta
	 */

	public void simulationInput(Complex[] dest, double fs, double f0, double delta) {
		
		double dif =  (delta * Math.PI) / 180;
		for (int i = 0; i < dest.length; i++) {
			dest[i]
					.setReal( Math
							.sin((2 * Math.PI * f0 * i) / fs + dif));
			dest[i].setimag(0) ;
		}
	}

	/**
	 * 得到相位角差XXX
	 * 
	 * @param src
	 * @param fs
	 * @param f0
	 * @return
	 */
	public double getDifferencePhase(Complex[] src, double fs, double f0) {
		double[] w = new double[2048];

		Complex[] temp = new Complex[2048];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = new Complex();
		}
		double df =  fs / src.length;
		int location = (int) (f0 / df);
		double alpha, alpha1;

		mwindow(w,1);
		window(src, w);
		msplfft(src,  -1);
		alpha = getPhase(src, location);

		simulationInput(temp, fs, f0, 0);
		window(temp, w);
		msplfft(temp,  -1);
		alpha1 = getPhase(temp, location);

		return (alpha - alpha1);
	}


	// /////////////////////////////////////////////////////////////////
	private final double[][] Ca10Fs2p56K = { { 0.2811f, 0.304f },
			{ 1.265f, 0.8199f }, { -1.8752f, 0.8867f }, { 1.5153f, 0.965f },
			{ -1.9808f, 0.9841f }, { -1.9948f, 0.9972f } };

	private final double[][] Cb10Fs2p56K = { { 0.2327f, 0.4556f, 0.2327f },
			{ 1.0f, 1.7722f, 1.0f }, { 1.0f, -1.9998f, 1.0f },
			{ 1.0f, 1.6733f, 1.0f }, { 1.0f, -1.9989f, 1.0f },
			{ 1.0f, -1.9983f, 1.0f } };

	public void ehpFilterSOS(double[] x, String szFC) {
		int ns = 6;
		double[] s = new double[x.length];
		double[] y = new double[x.length];

		for (int i = 0; i < ns; i++) {
			s[0] = x[0];
			s[1] = x[1] - Ca10Fs2p56K[i][0] * s[0];

			y[0] = Cb10Fs2p56K[i][0] * s[0];
			y[1] = Cb10Fs2p56K[i][0] * s[1] + Cb10Fs2p56K[i][1] * s[0];

			for (int j = 2; j < x.length; j++) {
				s[j] = x[j] - Ca10Fs2p56K[i][0] * s[j - 1] - Ca10Fs2p56K[i][1]
						* s[j - 2];
				y[j] = Cb10Fs2p56K[i][0] * s[j] + Cb10Fs2p56K[i][1] * s[j - 1]
						+ Cb10Fs2p56K[i][2] * s[j - 2];
			}
			x = y;
		}
	}
	
	public void PDAIntegrator(double[] x,double dt){
		double a1 = 1.07664f;
		double a2 = 0.47633f;
		double count = 0 ;
		double[] y = new double[x.length];
		double avg =  0 ;
		y[0] = a2 * x[0] * dt;
		y[1] = dt * (a2 * x[1] + a1 * x[0]);

		for(int i = 2; i < x.length; i++)
		{
			y[i] = dt * (a2 * x[i] + a1 * x[i-1] + a2 * x[i-2]) + y[i-2];
		}

		
		for (int i = 0; i < x.length; i++) {
			count +=x[i] ;
		}
		avg = count /x.length ;
		for(int i = 0; i < x.length; i++)
		{
			x[i] = y[i] - avg;
		}
		
	}

	//public double getFs() {
	//	return fs;
	//}
}
