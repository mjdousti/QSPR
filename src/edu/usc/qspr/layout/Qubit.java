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
import java.io.PrintWriter;

import edu.usc.qspr.RuntimeConfig;


public class Qubit {
	private String name;
	private Dimension pos;
	private Layout layout;

	public static enum Direction{
		Right, Left, Up, Down
	}
	
	public Qubit(String s, Dimension d, Layout l){
		name=new String(s);
		pos=new Dimension(d);
		layout=l;
	}
	
	public String getName(){
		return name;
	}
	
	public Dimension getPosition(){
		return pos;
	}
	
	public void turn(PrintWriter outputFile){
		if(RuntimeConfig.VERBOSE)
			outputFile.print("Turn "+getName()+" @("+getPosition().height+","+getPosition().width+") ");
	}
	
	public void move(Direction d, PrintWriter outputFile){
		Dimension prevPosition=new Dimension(pos);

		switch (d){
		case Right:
			pos.width+=1;
			break;
		case Left:
			pos.width-=1;
			break;
		case Up:
			pos.height-=1;
			break;
		case Down:
			pos.height+=1;
			break;
		}
		assert pos.width>=0;
		assert pos.width<layout.getLayoutSize().width;
		assert pos.height>=0;
		assert pos.height<layout.getLayoutSize().height;

		if (layout.getSquare(pos).getQubitsNo()==RuntimeConfig.CHANNEL_CAP){	
			return;
		}
		layout.free(prevPosition, this);
		layout.occupy(pos, this);
		
		if(RuntimeConfig.VERBOSE){
			outputFile.print("Move "+getName()+" ("+prevPosition.height+","+prevPosition.width+")->("+
								pos.height+","+pos.width+") ");
		}
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.equals(name);
	}
	
	//	Might be needed in the future
//	public void setPosition(Dimension d){
//		pos.height=d.height;
//		pos.width=d.width;
//	}
}
