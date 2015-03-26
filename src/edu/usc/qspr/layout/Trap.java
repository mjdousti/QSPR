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

public class Trap extends Square{
	public Trap(Dimension m, Dimension n, int step, Types t, Qubit.Direction channelDir) {
		super(m, n, step, t);
		this.channelDir=channelDir;
	}

	//TODO
	private Operation[] supportedOps;
	private Qubit.Direction channelDir;
	

	
	public Qubit.Direction getChannelDir(){
		return channelDir;
	}

	public Qubit getOtherQubit(String q){
		for (Qubit qubit : qubitSet) {
			if (!qubit.getName().equals(q))
				return qubit;
		}
		return null;
	}
	
}
