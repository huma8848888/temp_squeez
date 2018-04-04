package com.example.freq;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Complex implements Serializable{
	
	private double real ;
	
	private double imag ;
	
	public Complex(){
	}
	public Complex(double real ,double imag){
		this.real = real ;
		this.imag = imag ;
	}

	public double getReal() {
		return real;
	}

	public void setReal(double erealal) {
		this.real = erealal;
	}

	public double getimag() {
		return imag;
	}

	public void setimag(double imag) {
		this.imag = imag;
	}
	
	
}
