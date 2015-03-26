/*
 * 
 * Copyright (C) 2014 Mohammad Javad Dousti and Massoud Pedram, University of Southern California.
 * All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qspr.qasm;

public class Vertex extends Command implements Comparable<Vertex>{
	private int commandNo;
	private int priority=-1;
	private int readyInpts=0;
	private int level;
	private long Tcongestion;
//	private boolean ready;
//	private List<String> operands;
	
	Vertex(String c, int no, String ...ops){
		super(c, ops);
		commandNo=no;
		//sentinels are already ready
//		ready= (ops==null) ? true : false;
	}
	
	
	//***********Getters***********
	public int getLevel(){
		return level;
	}
	public int getReadyInpts(){
		return readyInpts;
	}
	public int getCommandNo(){
		return commandNo;
	}
	public int getPriority(){
		return priority;
	}
	public String getCommand(){
		return getName();
	}
//	public boolean isReady(){
//		return ready;
//	}	
	public boolean isSentinel(){
		return getName().equals("start")||getName().equals("end") ? true : false;
	}
//	public String getInstType(){
//		return instType;		
//	}


	//TODO: Should be added again if needed
//	public Qubit[] getOperands(){
//		return operands;
//	}
	
	//***********Setters***********	
	public void setLevel(int k){
		level=k;				
	}

	public void incReadyInpts(){
		readyInpts++;
	}
	public void setPriority (int p){
		priority=p;
	}
	
	public void addPriority (int p){
		priority+=p;
	}

//	public void setReady(boolean d){
//		ready=d;
//	}	
	public Vertex setCommandNo(int no){
		commandNo=no;
		return this;
	}
	
	public String toString() {
		String out=String.valueOf(commandNo)+") "+getName()+" ";
		for (int i = 0; i < getOperandsNumber(); i++) {
			if (i!=getOperandsNumber()-1)
				out+=getOperand(i)+", ";
			else
				out+=getOperand(i);
		}
		return out;
	}

	@Override
	public int compareTo(Vertex o) {
		if (priority>o.getPriority())
			return -1;
		else if (priority<o.getPriority())
			return 1;
		else{
			if (commandNo<o.getCommandNo())
				return -1;
			else if (commandNo>o.getCommandNo())
				return 1;
			else
				return 0;	
		}
			
	}
	
	public boolean hasOperand(String qubit){
		for (int i = 0; i < getOperandsNumber(); i++) {
			if (getOperand(i).equals(qubit))
				return true;
		}
		return false;
	}
}
