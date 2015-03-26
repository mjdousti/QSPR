/*
 * 
 * Copyright (C) 2014 Mohammad Javad Dousti and Massoud Pedram, University of Southern California.
 * All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qspr.layout;

import java.awt.Dimension;

import edu.usc.qspr.RuntimeConfig;
import edu.usc.qspr.layout.Layout.Types;

public class Channel extends Square{
//	private int capacity=0;
	private final int baseCost;
	private int currentCost;
	private boolean isHorizontal;
	private Junction v1;
    private Junction v2;

	
	public Channel(Dimension m, Dimension n, int step, Types t, boolean horizontal) {
		super(m, n, step, t);
		currentCost=baseCost=Layout.distance(m, n)+1;
		isHorizontal=horizontal;
	}

	public void setV1(Junction j){
		v1=j;
	}

	public void setV2(Junction j){
		v2=j;
	}

	public Junction getV1() {
        return v1;
    }

	public Junction getV2() {
        return v2;
    }


	
	public int getBaseCost(){
		return baseCost;
	}
	
	public int incWeight(){
		if (currentCost<(RuntimeConfig.CHANNEL_CAP)*baseCost){
			currentCost+=baseCost;
		}else if (currentCost<Integer.MAX_VALUE)
			currentCost=Integer.MAX_VALUE;
		else{
			System.out.println("\nAgain? Increase weight? :((");
			for( StackTraceElement ste : Thread.currentThread().getStackTrace() ) {
				 System.out.println( ste );
				}
			System.exit(-1);
		}
		return currentCost;
	}
	
	public int getWeight(){
		return currentCost;
	}

	public int decWeight(){
		if (currentCost==Integer.MAX_VALUE){
			currentCost=(RuntimeConfig.CHANNEL_CAP)*baseCost;
		}else if (currentCost>baseCost){
			currentCost-= baseCost;
		}else{
			System.out.println("currentCost: "+currentCost);
			System.out.println("\nAgain? Decrease weight? :((");
			for( StackTraceElement ste : Thread.currentThread().getStackTrace() ) {
				 System.out.println( ste );
				}
			System.exit(-1);
		}
		return currentCost;
	}

	
	public boolean isHorizontal(){
		return isHorizontal;
	}
	
	public Dimension getLeftBorder(){
		return a;
	}
	
	public Dimension getRightBorder(){
		return b;
	}
	
	//Buggy for one block channel
	public Dimension getLeftNeighbour(){
		if (isHorizontal)
			return new Dimension(getLeftBorder().width-1, getLeftBorder().height);
		else
			return new Dimension(getLeftBorder().width, getLeftBorder().height-1);
	}
	
	public Dimension getRightNeighbour(){
		if (isHorizontal)
			return new Dimension(getRightBorder().width+1, getRightBorder().height);
		else
			return new Dimension(getRightBorder().width, getRightBorder().height+1);
	}
	
}
