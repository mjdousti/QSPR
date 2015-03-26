/*
 * 
 * Copyright (C) 2014 Mohammad Javad Dousti and Massoud Pedram, University of Southern California.
 * All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qspr.router;

import java.awt.Dimension;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;


import edu.usc.qspr.RuntimeConfig;
import edu.usc.qspr.layout.Channel;
import edu.usc.qspr.layout.ChannelEdge;
import edu.usc.qspr.layout.Layout;
import edu.usc.qspr.layout.Qubit;
import edu.usc.qspr.layout.Trap;
import edu.usc.qspr.layout.Layout.Types;
import edu.usc.qspr.layout.Qubit.Direction;
import edu.usc.qspr.qasm.Vertex;

public class Path implements Comparable<Path>{
	private ArrayList<ChannelEdge> path;
	private Dimension src, dst;
	private Qubit qubit;
	private Vertex cmd;
	private Qubit.Direction dir=null;
	private long delay;
	private Layout layout;
	//	private int state;
	private Dimension indicator=null;
	private boolean finished;
	private boolean executionFinished;


	public Path(ArrayList<ChannelEdge> p, Dimension dst, Qubit q, Vertex command, long simTime, Layout l) {
		layout=l;
		qubit=q;
		this.dst=dst;
		this.src=qubit.getPosition();

		path=p;
		cmd=command;
		delay=simTime+updateDelay();
		finished=false;
		executionFinished=false;
	}

	public Dimension getDestination(){
		return dst;
	}

	public ArrayList<ChannelEdge> getPath(){
		return path;
	}


	private Direction turnToTrap(Dimension x){
		if (((dir==Direction.Left)||(dir==Direction.Right))&& x.width==dst.width && Math.abs(x.height-dst.height)==1){ 
			if (x.height-dst.height==1)
				return Direction.Up;
			else
				return Direction.Down;
		}
		else if (((dir==Direction.Down)||(dir==Direction.Up))&& x.height==dst.height && Math.abs(x.width-dst.width)==1){
			if (x.width-dst.width==1)
				return Direction.Left;
			else
				return Direction.Right;
		}else
			return dir;
	}

	public long updateDelay() {
		Dimension x=qubit.getPosition();
		switch (layout.getSquareType(x)){
		case Channel:
			//TODO: NOT SURE
			if (path.size()==1 && dir!=turnToTrap(x) ||dir==null){
				return delay+=layout.getOpDelay("Turn");
			}else{
				return delay+=layout.getMoveDelay();
			}
		case Junction:
			if (isTurnNeeded()){
				return delay+=layout.getOpDelay("Turn");
			}else{
				return delay+=layout.getMoveDelay();
			}
		case Trap:
			if (qubit.getPosition().equals(dst)){
				if (((Trap)layout.getSquare(qubit.getPosition())).getQubitsNo() == cmd.getOperandsNumber()){
					return delay+=layout.getOpDelay(cmd.getName());
				}else{
					if (RuntimeConfig.DEBUG)
						System.out.println("Jumped!");
					finished=true;
					return delay=0;
				}
			}
			else{
				return delay+=layout.getMoveDelay();
			}
			
		}

		return delay;
	}

	public long getDelay(){
		return delay;
	}

	private boolean isTurnNeeded(){
		if (path.isEmpty()){
			System.out.println(qubit);
			System.out.println("dst: "+dst.height+"x"+dst.width);
			System.out.println("VLVLVLLV");
			System.exit(-1);
		}

		if (path.get(0).getChannel()==null)
			return true;
		else
			return false;
	}


	public ChannelEdge nextMove(PrintWriter outputFile){
		ChannelEdge removedPath=null;
		finished=false;
		switch (layout.getSquareType(qubit.getPosition())){
		case Channel:
			if (path.size()==1 && dir!=turnToTrap(qubit.getPosition())){
				qubit.turn(outputFile);
				dir=turnToTrap(qubit.getPosition());
			}else if (dir==null){
				qubit.turn(outputFile);
				determineDirection();
			}else
				qubit.move(determineDirection(), outputFile);

			//TODO: reaching the final trap (take care, if it is the first qubit)
			if (layout.isJunction(qubit.getPosition())){
				//removing the previous channel
				removedPath=path.remove(0);
			}else if  (layout.isTrap(qubit.getPosition())){
				removedPath=path.remove(0);
			}
			updateDelay();
			break;
		case Junction:
			if (isTurnNeeded()){
				//removing the turn edge
				path.remove(0);
				qubit.turn(outputFile);
			}else{
				//TODO: should be improved
				qubit.move(dir=determineDirection(), outputFile);
			}
			updateDelay();
			break;		
		case Trap:
			//Reached at the destination
			if (qubit.getPosition().equals(dst)){
				if (((Trap)layout.getSquare(qubit.getPosition())).getQubitsNo() == cmd.getOperandsNumber()){
					//TODO: Should be moved to the layout. Layout is responsible of doing quantum operations 
					if(RuntimeConfig.VERBOSE){
						outputFile.print("'"+getFullCommand()+"'"+" @("+qubit.getPosition().height+","+qubit.getPosition().width+") \t\t");
					}
					executionFinished=true;
				}//This check is not needed! Just to be safe!
				else if (((Trap)layout.getSquare(qubit.getPosition())).getQubitsNo() > cmd.getOperandsNumber()){
					System.out.println("Fatal error in routing! More qubits than needed were routed to one trap.");
					System.exit(-1);
				}
				finished=true;
				return removedPath;
			}//Just started the journey
			else{
				qubit.move(determineDirection(), outputFile);
				updateDelay();
			}		
		}
		return removedPath;
	}

	public boolean isFinished(){
		return finished;
	}

	public boolean isExecutionFinished(){
		return executionFinished;
	}

	public String getCommand(){
		return cmd.getCommand();
	}

	public String getFullCommand(){
		String s=cmd.getCommand()+" "+cmd.getOperand(0);
		for (int i = 1; i < cmd.getOperandsNumber(); i++) {
			s+=", "+cmd.getOperand(i);
		}
		return s;
	}


	public Dimension getQubitPosition(){
		return qubit.getPosition();
	}

	public Qubit getQubit(){
		return qubit;
	}

	public int getOperandsNumber(){
		return cmd.getOperandsNumber();
	}

	public String getOperand(int index){
		return cmd.getOperand(index);
	}

	private Qubit.Direction determineDirection(Dimension src, Dimension dst){
		if (src.height==dst.height){
			if (src.width>dst.width)
				return Qubit.Direction.Left;
			else
				return Qubit.Direction.Right;
		}else if (src.width==dst.width){
			if (src.height>dst.height)
				return Qubit.Direction.Up;
			else
				return Qubit.Direction.Down;

		}
		System.out.println();
		System.out.println(cmd);
		System.out.println(qubit);
		System.err.println("Fatal error! Incorrect routing!");
		System.exit(-1);
		return Qubit.Direction.Down;
	}

	private Qubit.Direction determineDirection(){
		Dimension current=qubit.getPosition();

		switch (layout.getSquareType(current)){
		case Channel:
			if (path.size()==1 && (current.width==dst.width || current.height==dst.height)){
				indicator=dst;
				return dir=determineDirection(current, indicator);
			}else if (dir!=null){
				return dir;
			}//source and destination reside on a channel
			else if (path.size()==1 && dir==null){
				if (dst.height-1==current.height){
					indicator =new Dimension(dst);
					indicator.height--;
				}else if (dst.height+1==current.height){
					indicator =new Dimension(dst);
					indicator.height++;
				}else if (dst.width-1==current.width){
					indicator =new Dimension(dst);
					indicator.width--;
				}else if (dst.width+1==current.width){
					indicator =new Dimension(dst);
					indicator.width++;
				}
				return dir=determineDirection(current, indicator);
			}else{
				if (path.size()==1){
					System.out.println(cmd);
					System.out.println(qubit);
					System.exit(-1);
				}

				if (path.get(0).getV1()==path.get(1).getV1() || path.get(0).getV1()==path.get(1).getV2()){
					indicator=path.get(0).getV1().getPosition();
				}else
					indicator=path.get(0).getV2().getPosition();
				return dir=determineDirection(current, indicator);
			}

		case Junction:
			if (path.get(0).getV1().getPosition().equals(current))
				indicator=path.get(0).getV2().getPosition();
			else
				indicator=path.get(0).getV1().getPosition();
			return dir=determineDirection(current, indicator);

		case Trap:
			if (qubit.getPosition().equals(dst))
				return null;
			else{
				return ((Trap)(layout.getSquare(current))).getChannelDir();
			}			
		}
		return null;
	}

	@Override
	public int compareTo(Path o) {
		if (getDelay()<o.getDelay())
			return -1;
		else if (getDelay()>o.getDelay())
			return 1;
		else{
			return cmd.compareTo(o.getVertex());
		}
	}

	public Vertex getVertex(){
		return cmd;
	}

	public String toString(){
		String output=new String();
		
		for (int i = 0; i < path.size(); i++) {
			output+=i+": "+ path.get(i)
					+System.getProperty("line.separator");
		}
		return output;
	}
}
