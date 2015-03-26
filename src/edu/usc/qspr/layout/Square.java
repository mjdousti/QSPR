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
import java.util.HashSet;
import java.util.Set;

import edu.usc.qspr.layout.Layout.Types;

public class Square {
	protected Dimension a, b;
	//TODO: no use for step. should be removed
	private int step;
	private Types type;
	protected Set<Qubit> qubitSet=new HashSet<Qubit>();
	private boolean used=false;

	
	public Square(Dimension m, Dimension n, int step, Types t) {
		a=new Dimension(m);
		b=new Dimension(n);
		this.step=step;
		type=t;
	}
	
	public void setUsed(boolean status){
		used=status;
		if (status==false)
			qubitSet.clear();
	}
	
	public boolean isUsed(){
		return used;
	}
		
	
	public Dimension getPosition(){
		return a;
	}
	
	public int getLength(){
		if (a.height==b.height)
			return Math.abs(a.width-b.width);
		else
			return Math.abs(a.height-b.height);
	}
	
	public Types getSquareType(){
		return type;
	}
	
	public int getStep(){
		return step;
	}
	
	
	public boolean isOccupied(){
		return !qubitSet.isEmpty();
	}
	
	
	public void addQubits(Qubit q){
		setUsed(true);
		qubitSet.add(q);
	}

	public boolean removeQubit(Qubit q){
		return qubitSet.remove(q);
	}
	
	public int getQubitsNo(){
		return qubitSet.size();
	}
	
	public Set<Qubit> getQubitSet(){
		return qubitSet;
	}
	
	public void printQubitSet(){
		System.out.println("Qubit List @"+getPosition().height+"x"+getPosition().width);
		for (Qubit q : qubitSet) {
			System.out.println(q.getName());
		}
	}
	
}
