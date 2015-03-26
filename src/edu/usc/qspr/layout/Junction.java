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

import edu.usc.qspr.layout.Layout.Types;

public class Junction extends Square{

//	private Channel[] directChannels;
//	public Junction(Dimension m, Dimension n, int step, Types t, Channel []c) {
//		super(m, n, step, t);
//		directChannels=c;
//	}
	

	public Junction(Dimension m, Dimension n, int step, Types t, Direction d) {
		super(m, n, step, t);
		direction=d;
	}

	enum Direction{
		Horizontal, Vertical, Old
	}
	
	private Direction direction;
	
	public Junction(Dimension m, Dimension n, int step, Types t) {
		super(m, n, step, t);
	}
	
//	public Junction(Junction j, Channel[] c){
//		super(j.getPosition(), j.getPosition(), j.getStep(), Types.Junction);
//		directChannels=c;
//	}
	

	public Junction(Junction j, Direction d){
		super(j.getPosition(), j.getPosition(), j.getStep(), Types.Junction);
		direction=d;
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		Channel[] temp=((Junction)obj).getChannels();
//		boolean equal=false;
//		for (int i = 0; i < directChannels.length; i++) {
//			for (int j = 0; j < temp.length; j++) {
//				if (temp[j]==null && temp[j]==directChannels[i])
//					equal=true;
//			}
//		}
//		
//		if (super.getPosition()== ((Junction)obj).getPosition() && equal)
//			return true;
//		else
//			return false;
//	}
	
//	public Channel[] getChannels(){
//		return directChannels;
//	}

	public Direction getDirection(){
		return direction;
	}
}
