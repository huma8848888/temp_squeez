package com.example.master.temp_squeez;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import org.apache.http.util.EncodingUtils;

import com.example.freq.DataBeCounted;
import com.example.uart.ChartData;
import com.example.uartdemo.SerialPort;
import com.example.uartdemo.Tools;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;


public class MainActivity extends Activity implements OnClickListener  {

	/** UI  **/
	private EditText editwd;
	private Button butWDfslup,butWDfsldown ;
	private Button butWDCL ;
	private Button butZDCL,butZDpp ;
	private Switch caseHL ;
	private Spinner spZDLB = null;
	
	/** serialport **/
	private SerialPort mSerialPort ;
	private InputStream is ;
	private OutputStream os ;

	/** recv Thread **/
	private RecvThread recvThread ;
	private boolean isserial = false ;	
	private boolean isHexSend= false ;	
	private String lastdata,ppdata;

	private String cmd="HR" ;//发送命令：HR读振动编号，0b0b读温度，0Fxx振动
	private String lastcmd="xxxxx" ;
	int fsl=95;
	private int curzdlb=0;//读保存的配置文件
	private int curfreq=0; //读保存的配置文件
	private String djtag="";
	//pp
	private int ppfreq=1280;//5120;  <1280,5120,12800,25600>
	private int pplen=1024;	//           1024 2048 4096 8192
	private double[] ppsour;	
	private int recIndex;
	private byte[] recBuff;
	public ChartData chart1=new ChartData(this);
	public ChartData chart2=new ChartData(this);
	
	private void initdata(){
		recIndex=0;
		recBuff = new byte[pplen*2];
		ppsour = new double[pplen];
	}
	
	public String  getplcmd(){
		String res="";
		switch (ppfreq) {
		case 1280:
			res="01";
			break;
		case 5120:
			res="02";
			break;
		case 12800:
			res="03";
			break;
		case 25600:
			res="04";
			break;
		default:
			break;
		}		
		return res;
	}
	public String  getlencmd(){
		String res="";
		switch (pplen) {
		case 1024:
			res="01";
			break;
		case 2048:
			res="02";
			break;
		case 4096:
			res="03";
			break;
		case 8192:
			res="04";
			break;
		default:
			break;
		}		
		return res;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initdata();
		editwd= (EditText) findViewById(R.id.editTextInfo);
		butWDCL= (Button) findViewById(R.id.butWD);
		butWDfslup=(Button) findViewById(R.id.butwdfup);
		butWDfsldown=(Button) findViewById(R.id.butwdfdown);
		butZDCL= (Button) findViewById(R.id.butZD);
		butZDpp= (Button) findViewById(R.id.butZDpp);

		butWDfslup.setOnClickListener(this);
		butWDfsldown.setOnClickListener(this);
		butWDCL.setOnClickListener(this);
		butZDCL.setOnClickListener(this);
		butZDpp.setOnClickListener(this);
		
		caseHL= (Switch) findViewById(R.id.caseHL);
		spZDLB = (Spinner) findViewById(R.id.selectLB); 
		spZDLB.setAdapter(ArrayAdapter.createFromResource( this, R.array.selectzdlb,android.R.layout.simple_spinner_dropdown_item));

		//读上次保存的配置信息,curzdlb是振动类型，curfreq是振动频率
		curzdlb=readZDData("zdlb");
		curfreq=readZDData("freq");
		///////////////////////////////////////
		
		if(curfreq==1) caseHL.setChecked(true);
		spZDLB.setSelection(curzdlb);

		//读上次保存的配置信息，温度辐射率
		fsl=readZDData("fsl");
		if(fsl<10) fsl=95;
		butWDCL.setText("温度测量ε0."+fsl);
		
		djtag=getIntent().getStringExtra("call");
		if (djtag!=null){
			if(djtag.equals("速度")) spZDLB.setSelection(0);
			if(djtag.equals("加速度")) spZDLB.setSelection(1);
			if(djtag.equals("位移")) spZDLB.setSelection(2);
		}

		this.open();//打开服务
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		send();//发送默认命令HR
	}

	//振动协议命令
	public String  getzdcmd(){
		String res="";
		if (caseHL.isChecked()){//H 
			if(spZDLB.getSelectedItem().toString().equals("加速度"))	res="0F21";
			if(spZDLB.getSelectedItem().toString().equals("速度"))	res="0F22";
			if(spZDLB.getSelectedItem().toString().equals("位移"))	res="0F23";
		}
		else{
			if(spZDLB.getSelectedItem().toString().equals("加速度"))	res="0F11";
			if(spZDLB.getSelectedItem().toString().equals("速度"))	res="0F12";
			if(spZDLB.getSelectedItem().toString().equals("位移"))	res="0F13";
		}
		return res;
	}
	
	public void ppfx(){
		//isfreq=true;
		PopupWindow popWin = null; 
		View popView=LayoutInflater.from(this).inflate(R.layout.freqchart,null);
		popWin = new PopupWindow(popView,RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT,true);  // ʵ������������
		popWin.setTouchable(true);
		popWin.setOutsideTouchable(true);
		popWin.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
		popWin.showAtLocation(popView,  Gravity.CENTER, 0, 0);  // 显示弹出窗口
		popWin.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				//isfreq=false;
				cmd="";
				System.out.println("fft back!");
				if (djtag!=null){
					 Broaddata();
					 finish();
				}
			}
		});
		chart1.buildchart(popView, R.id.sychart);
		chart2.buildchart(popView, R.id.ppchart);
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.butwdfup://设置温度辐射率加1
			if(fsl>=99) break;
			fsl=fsl+1;
			
			isserial=false;isHexSend=false;
			lastcmd="DD0."+fsl ;
			if(!cmd.equals("0b0b"))	send(lastcmd);
			Toast.makeText(this, "辐射率"+lastcmd,Toast.LENGTH_SHORT).show();
			break;
		case R.id.butwdfdown://设置温度辐射率减1
			if(fsl<=10) break;
			fsl=fsl-1;
			
			isserial=false;isHexSend=false;
			lastcmd="DD0."+fsl ;
			if(!cmd.equals("0b0b"))	send(lastcmd);
			Toast.makeText(this, "辐射率"+lastcmd, Toast.LENGTH_SHORT).show();
			break;
		case R.id.butWD://温度测量
			//isserial=true;isHexSend=true;
			//cmd="0b0b" ;
			isserial=false;isHexSend=false;
			lastcmd="DD0."+fsl ;
			cmd="0b0b" ;
			send(lastcmd);
			Toast.makeText(this, "温度测量"+lastcmd, Toast.LENGTH_SHORT).show();
			break;
		case R.id.butZD://振动测量
			isserial=true;isHexSend=true;
			cmd=getzdcmd();
			Toast.makeText(this, cmd, Toast.LENGTH_SHORT).show();
			send();
			break;
		case R.id.butZDpp://pp测量
			if(recIndex>0)return;
			ppfx();
			recIndex=0;
			isserial=true;isHexSend=true;
			if(cmd.equals(""))	{
				cmd="0e"+getplcmd()+getlencmd();
				lastcmd="xxxxxxxx";
				send();
				}
			else
				lastcmd="0e"+getplcmd()+getlencmd();
			Toast.makeText(this, cmd, Toast.LENGTH_SHORT).show();
			//send();
			break;
		default:
			break;
		}		
	}	

	private void Broaddata() {
		//广播返回结果数据//////////
    	Intent intent=new Intent();         
    	intent.setAction("com.bjzc.wd"); 
    	intent.putExtra("data",lastdata);     
    	intent.putExtra("ppdata",ppdata); 
    	intent.putExtra("type",cmd);  
    	sendBroadcast(intent); 
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if (keyCode == KeyEvent.KEYCODE_BACK)	{	
			 Broaddata();
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onDestroy() {
		//保存当前配置信息,curzdlb是振动类型，curfreq是振动频率，fsl是温度系数
		if(caseHL.isChecked())curfreq=1; else curfreq=0;
		curzdlb=spZDLB.getSelectedItemPosition();
		writeFileData("freq",curfreq);
   	    writeFileData("zdlb",curzdlb);
   	    writeFileData("fsl",fsl);
   	    
		this.close();//关闭服务
		super.onDestroy();
	}


	private void open(){
		try {
			mSerialPort = new SerialPort(14, 9600, 0);//230400 ,9600,
		}catch (Exception e) {			
			Toast.makeText(this, "SerialPort init fail!!", Toast.LENGTH_SHORT).show();
			return;
		}
		is = mSerialPort.getInputStream();
		os = mSerialPort.getOutputStream();
		
		mSerialPort.psam_poweron();
		mSerialPort.power_5Von();
		recvThread = new RecvThread();
		recvThread.start();
		Toast.makeText(this, "SerialPort open success", Toast.LENGTH_SHORT).show();
	}

	private void close(){
		if(recvThread != null){
			recvThread.interrupt();
		}
		if(mSerialPort != null){
			mSerialPort.psam_poweroff();
			mSerialPort.power_5Voff();
			try {
				is.close();
				os.close();
			} catch (IOException e) {
			}
			mSerialPort.close(14);
		}
	}

	/**
	 * 后台服务自动返回数据
	 */
	private class RecvThread extends Thread{
		@Override
		public void run() {
			super.run();
			try {
			while(!isInterrupted()){
				int size = 0;
				byte[] buffer = new byte[1024];
				if(is == null){
					return;
				}
				size = is.read(buffer);
				if(size > 0){
					onDataReceived(buffer, size);
				}
				Thread.sleep(10);
			}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * add recv data on UI
	 * @param buffer
	 * @param size
	 */
	private void onDataReceived(final byte[] buffer, final int size){
		runOnUiThread(new Runnable() {		
			@Override
			public void run() {
				  if (size==1) return;//打开串口后有一个垃圾数据。
				  if(cmd.length()<1) return;
				  if(lastcmd.substring(0, 2).equals("0e")) {					 
					  send(lastcmd);
					  cmd=lastcmd;
					  lastcmd="xxxxxx";
					  return;
				  }
				  if(cmd.substring(0, 2).equals("0e"))
				  {
					    //System.out.println(buffer[size-2]+","+buffer[size-1]+"长度"+recIndex+","+size);
						int copylen=0;
						if(buffer[size-2]==-1&&buffer[size-1]==-1&&((recIndex+size)>=pplen*2))//一个频谱的最后一批数据
							copylen=size-2;
						else
							copylen=size;

						System.arraycopy(buffer,0,recBuff,recIndex,copylen);
						recIndex=recIndex+copylen;

					    if(recIndex==pplen*2&&buffer[size-2]==-1&&buffer[size-1]==-1) {//一个频谱结束
						    recIndex=0;
						    fft();
					    }
					    return;
				   }
				  
				   String recv = new String(buffer, 0 , size).trim().replace("\r\n","");
				   if (Tools.isNumeric(recv)) lastdata=recv;
				   
				   if(cmd.equals("HR")){ 
					   cmd="";
					   setTitle(getTitle()+recv);
					   //程序启动后，自动测量
					   if(djtag!=null&&djtag.equals("温度"))	   butWDCL.performClick();
					   if(djtag!=null&&!djtag.equals("温度"))	   butZDCL.performClick();
				   }
				   else
					   editwd.setText(recv);
				   
				   if(lastcmd.substring(0, 2).equals("DD")) {
					   System.out.println(lastcmd+"HH"+recv);
					   if(lastcmd.equals("DD"+recv)){
						   butWDCL.setText("温度测量ε"+recv);
						   lastcmd="xxxxxx";
						   if(cmd.equals("0b0b")){
						   cmd="0b0b" ;isHexSend=true;isserial=true;}
					   }
					   else {
						   isserial=false;isHexSend=false;
						   send(lastcmd); 
					   }
				   }
				   
				   if(isserial) send();
			}
		});
	}
	
	private void fft(){
		ppdata="";
		for (int i = 0; i < pplen; i++) {
			int tmp=recBuff[i*2]&0xff;
			int tmp1=recBuff[i*2+1]&0xff;
			int ad=tmp*256+tmp1;
			if (ad>2047)  
				ppsour[i]=-(4096-ad);
			else 
				ppsour[i]=ad; 
			
			float VolCoef=(float)1/4096;
			ppsour[i]=ppsour[i]*VolCoef;  //缩小时域值
			//save
			ppdata=ppdata+String.format("%.3f", ppsour[i])+",";
		}


			chart1.updateChartUI(ppsour);	
			DataBeCounted fftd=new DataBeCounted();
	    	fftd.getFFTdata(ppsour,chart2,ppfreq);//ppfreq ,这个应该和数量对应，如果数量是1024，这个地方必须是1280
	    	send();
		
	}

	//发送命令
	private void send(String cmd){
		try {
			if(isHexSend) 	
				os.write(Tools.HexString2Bytes(cmd));
			else
				os.write(cmd.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	//发送命令
	private void send(){
		try {
			if(isHexSend) 	
				os.write(Tools.HexString2Bytes(cmd));
			else
				os.write(cmd.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	//读写文件
	public void writeFileData(String fileName,int message){ 
	       try{ 
	        FileOutputStream fout =openFileOutput(fileName, MODE_PRIVATE);
	        byte [] bytes =String.valueOf (message).getBytes(); 
	        fout.write(bytes); 
	        fout.close(); 
	        } 
	       catch(Exception e){ 
	        e.printStackTrace(); 
	       } 
	 }
	 public int readZDData(String fileName){ 
	        int res=1; 
	        try{ 
	         FileInputStream fin = openFileInput(fileName); 
	         int length = fin.available(); 
	         byte [] buffer = new byte[length]; 
	         fin.read(buffer);     
	         String s = EncodingUtils.getString(buffer, "UTF-8"); 
	         res=Integer.parseInt(s);
	         fin.close();     
	        } 
	        catch(Exception e){ 
	         e.printStackTrace(); 
	        } 
	        return res; 
	}
}

