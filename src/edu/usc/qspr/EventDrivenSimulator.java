/*
 * 
 * Copyright (C) 2014 Mohammad Javad Dousti and Massoud Pedram, University of Southern California.
 * All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qspr;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.util.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import edu.usc.qspr.layout.Channel;
import edu.usc.qspr.layout.ChannelEdge;
import edu.usc.qspr.layout.Junction;
import edu.usc.qspr.layout.Layout;
import edu.usc.qspr.layout.Qubit;
import edu.usc.qspr.layout.Trap;
import edu.usc.qspr.qasm.Vertex;
import edu.usc.qspr.router.BasePath;
import edu.usc.qspr.router.Path;
import edu.usc.qspr.scheduler.ReadyQueue;
import edu.usc.qspr.scheduler.WaitingQueue;
/**
 * @author Mohammad Javad
 *
 */
public class EventDrivenSimulator {
	private PrintWriter outputFile;
	private long simTime=0;
	public enum Scheduling{
		ASAP, ALAP
	}

	Layout layout;
	WaitingQueue waitQueue=new WaitingQueue();
	List<Vertex> busyInsts=new ArrayList<Vertex>();
	PriorityQueue<Path> issueQueue=new PriorityQueue<Path>();;
	ReadyQueue readyQueue;
	List<Vertex> commands;
	
	
	public EventDrivenSimulator(Layout layout, List<Vertex> cmds, PrintWriter outputFile) {
		this.layout=layout;
		this.outputFile = outputFile;
		commands=cmds;
	}
	
	
	/**
	 * Schedules the instructions in the commands list. It clears the <i>waitQueue</i>, <i>readyQueue</i>, and <i>issueQueue</i>. 
	 * Then it reschedules instructions in the <i>waitQueue</i> and <i>readyQeue</i>. 
	 */
	public void schedule(){
		waitQueue.clear();
		issueQueue.clear();
		
		for (Vertex v : commands) {
			if (v.isSentinel())
				continue;
			for (int i = 0; i < v.getOperandsNumber(); i++) {
				v.setReadyStatus(i, false);
				if (!waitQueue.containsKey(v.getOperand(i))){
					waitQueue.put(v.getOperand(i), new LinkedList<Vertex>());
				}
				waitQueue.get(v.getOperand(i)).add(v);
			}
		}
	
		issueQueue.clear();
		readyQueue=new ReadyQueue(waitQueue);

		if (RuntimeConfig.DEBUG){
			System.out.println(waitQueue.toString());
			System.out.println(readyQueue.toString());
		}
	}
	


	/**
	 * Prints the issued queue in a human readible format.
	 */
	public void printIssuedQueue(){
		for (Iterator<Path> iterator = issueQueue.iterator(); iterator.hasNext();) {
			System.out.println(iterator.next().getFullCommand());
			
		}
	}
	
	
	/**
	 * Computes the Manhattan distance between two points.
	 *
	 * @param a first given point
	 * @param b second given point
	 * @return the computed distance
	 */
	private int distance(Dimension a, Dimension b){
		return Math.abs(a.height-a.height)+Math.abs(a.width-b.width);
	}

	
	private ChannelEdge getChannelEdge(Channel c){
		Junction j1;
		Junction j2;

		j1=c.getV1();
		j2=c.getV2();
		
		return layout.getGraph().getEdge(j1, j2);
	}
	
	private ArrayList<ChannelEdge> findPath(Dimension src, Dimension dst){
		ArrayList<ChannelEdge> list;
		if (layout.getNearestChannel(src)==layout.getNearestChannel(dst)){
			list=new ArrayList<ChannelEdge>();
			list.add(getChannelEdge(layout.getNearestChannel(src)));
		}else{
			list = (ArrayList<ChannelEdge>) DijkstraShortestPath.findPathBetween(layout.getGraph(), 
					(Junction)layout.getNearestJunction(src), (Junction)layout.getNearestJunction(dst));
			
			//If the route contains the channel where the source or destination resides, it must be removed
			if (list.size()>0 && list.get(0).getChannel()==layout.getNearestChannel(src))
				list.remove(0);
			else if (list.size()>1 && list.get(0).getChannel()==null && list.get(1).getChannel()==layout.getNearestChannel(src)){
				list.remove(0);
				list.remove(0);
			}
	
			
			if (list.size()>0 && list.get(list.size()-1).getChannel()==layout.getNearestChannel(dst))
				list.remove(list.size()-1);
			else if (list.size()>1 && list.get(list.size()-1).getChannel()==null && list.get(list.size()-2).getChannel()==layout.getNearestChannel(dst)){
				list.remove(list.size()-1);
				list.remove(list.size()-1);					
			}
			list.add(0, getChannelEdge(layout.getNearestChannel(src)));
			list.add(getChannelEdge(layout.getNearestChannel(dst)));

		}
		
		 
	
		return list;		
	}
	
	/**
	 * Give a the best route between the operands of instruction v.
	 *
	 * @param v the given instruction
	 * @param simTime the simulation time
	 * @return list of channelEdges participating in the computed path. Returns null if no route could be found. 
	 */
	private List<Path> router(Vertex v, long simTime){
		boolean newTrapReserved=false;
		LinkedList<Path> pathList=new LinkedList<Path>();		
		LinkedList<Dimension> src=new LinkedList<Dimension>();
		LinkedList<Dimension> dst=new LinkedList<Dimension>();
		Dimension temp1, temp2;
		Qubit q = null;
		src.add(layout.getQubit(v.getOperand(0)).getPosition());
		
		//TODO: generalize for n-qubit operations
		if (v.getOperandsNumber()==2){
			dst.add(layout.getQubit(v.getOperand(1)).getPosition());
			temp1=layout.getNearestFreeTrap(new Dimension((src.getFirst().width+dst.getFirst().width)/2, (src.getFirst().height+dst.getFirst().height)/2), false).getPosition();
			
			if ((((Trap)layout.getSquare(dst.getFirst())).getQubitsNo()<2 
					&& distance(src.getFirst(), dst.getFirst())<distance(src.getFirst(), temp1)+distance(temp1, dst.getFirst()))){
				q=layout.getQubit(v.getOperand(0));
			}else if (((Trap)layout.getSquare(src.getFirst())).getQubitsNo()<2 
					&& distance(src.getFirst(), dst.getFirst())<distance(src.getFirst(), temp1)+distance(temp1, dst.getFirst())){
				temp2=dst.remove();
				dst.add(src.remove());
				src.add(temp2);
				q=layout.getQubit(v.getOperand(1));
			}else{
				temp2=dst.remove();
				layout.assignLastTrap(temp1);
				dst.add(temp1);
				dst.add(dst.getFirst());
				src.add(temp2);
				q=null;
				newTrapReserved=true;
			}
		}
		else{
			q=layout.getQubit(v.getOperand(0));
			if  (((Trap)(layout.getSquare(src.getFirst()))).getQubitsNo()==1){
				dst=src;
			}else{
				newTrapReserved=true;
				dst.add(layout.getNearestFreeTrap(src.getFirst(), true).getPosition());
			}
		}
		ArrayList<ChannelEdge> path;
//		Path path
		for (int i = 0; i < dst.size(); i++) {
			 
			if (src.get(i)==dst.get(i)){
				//source and destinations are on the same channel
				path=new ArrayList<ChannelEdge>();
			//source and destinations are on different channels and need to be routed
			}else{
				
				path = findPath(src.get(i), dst.get(i));
				
				for (int k = 0; k < path.size(); k++) {
					if (path.get(k).getWeight()==Integer.MAX_VALUE){
						for (int j = 0; j < pathList.size(); j++) {
							freeChannels(pathList.get(j).getPath());
						}
						if (newTrapReserved)
							layout.free(dst.get(0), null);							
						return null;
					}
				}
				//TODO mark the fist and the last channels as busy as well
				busyChannels(path);

			}
			if (q==null)
				pathList.add(new Path(path, dst.get(i), layout.getQubit(v.getOperand(i)), v, simTime, layout));
			else{
				pathList.add(new Path(path, dst.get(i), q, v, simTime, layout));
			
			if (RuntimeConfig.DEBUG){
				System.out.println(pathList.getLast());
			}
			}
		}
		

		
		return pathList;
	}
	
	
	/**
	 * Free the channels only one degree. 
	 *
	 * @param list the list of channelEdges participating in the path
	 */
	private void freeChannels(List<ChannelEdge> list){
		ChannelEdge c;
		for (Iterator<ChannelEdge> iterator = list.iterator(); iterator.hasNext();) {
			c=iterator.next();
			freeChannel(c);
		}
	}
	
	private void freeChannel(ChannelEdge c){
		if (c!=null && c.getChannel()!=null){
			layout.getGraph().setEdgeWeight(c, c.decWeight());
		}
	}

	/**
	 * Busy the channels only one degree. 
	 *
	 * @param  list the list of channelEdges participating in the path
	 */
	private void busyChannels(List<ChannelEdge> list){
		ChannelEdge c;
		for (Iterator<ChannelEdge> iterator = list.iterator(); iterator.hasNext();) {
			c=iterator.next();
			busyChannel(c);
		}
	}

	private void busyChannel(ChannelEdge c){
		//do not change the psodu-edges (turns)
		if (c.getChannel()!=null){
			layout.getGraph().setEdgeWeight(c, c.incWeight());
		}
	}
	
	public long baseSimulate(){
		PriorityQueue<BasePath> issueQueue=new PriorityQueue<BasePath>();;
		Set<Vertex> initList=readyQueue.getNext();
		BasePath temp0;
		simTime=0;

		
		for (Iterator<Vertex> iterator = initList.iterator(); iterator.hasNext();) {
			Vertex v=iterator.next();
			issueQueue.add(new BasePath(v, simTime, layout));			
		}

		while (!readyQueue.isEmpty() || !issueQueue.isEmpty()){
			simTime=issueQueue.peek().getDelay();
			if(RuntimeConfig.VERBOSE)
				outputFile.print(System.getProperty("line.separator")+"SimTime:"+simTime+"\t");
			
			do{
				temp0=issueQueue.remove();
				if(RuntimeConfig.VERBOSE)
					outputFile.print(temp0.getVertex()+"\t");
				LinkedList<Vertex> vTemp=readyQueue.getNext(temp0.getVertex());

				for (int i = 0; i < vTemp.size(); i++) {
					issueQueue.add(new BasePath(vTemp.get(i), simTime, layout));
				}
			}while(!issueQueue.isEmpty() && simTime==issueQueue.peek().getDelay());
		}
		if (RuntimeConfig.VERBOSE)
			outputFile.println();	//adding a new line at the end of the trace	
		return simTime;
	}

	
	/**
	 * Event driven simulator.
	 *
	 * @return the length of simulation in &microsec
	 */
	public long simluate(){
		ChannelEdge freedChannel;
		Path temp0;
		simTime=0;
		List<Path> paths;
		
		PriorityQueue<Vertex> initList=new PriorityQueue<Vertex>(readyQueue.getNext());
		
		for (Iterator<Vertex> iterator = initList.iterator(); iterator.hasNext();) {
			Vertex v=iterator.next();
			paths=router(v, simTime);

			if (paths!=null)
			{
				issueQueue.addAll(paths);
			}else{
				busyInsts.add(v);
			}
		}

		while (!readyQueue.isEmpty() || !issueQueue.isEmpty()){
			//For debugging
			if (issueQueue.isEmpty() ){
				System.out.println(System.getProperty("line.separator")+"Fatal Error: No more instruction to issue!");
//				System.out.println(readyQueue);
//				System.out.println(waitQueue);
				System.exit(-1);
			}
			
			simTime=issueQueue.peek().getDelay();
			if(RuntimeConfig.VERBOSE)
				outputFile.print(System.getProperty("line.separator")+"SimTime:"+simTime+"\t");

			do{
				temp0=issueQueue.remove();
				freedChannel= temp0.nextMove(outputFile);
				freeChannel(freedChannel);
				if (freedChannel!=null && !busyInsts.isEmpty()){
					for (int i = 0; i < busyInsts.size(); i++) {
						Vertex bi=busyInsts.get(i);
						paths=router(bi, simTime);
						if (paths!=null){
							busyInsts.remove(i);
							issueQueue.addAll(paths);
							i--;
						}
					}
				}
				if (!temp0.isFinished()){
					issueQueue.add(temp0);
				}else if (temp0.isExecutionFinished()){
					LinkedList<Vertex> vTemp=readyQueue.getNext(temp0.getVertex());

					for (int i = 0; i < vTemp.size(); i++) {
						paths=router(vTemp.get(i), simTime);
						if (paths!=null)
						{
							issueQueue.addAll(paths);
						}else{
							busyInsts.add(vTemp.get(i));
//								System.out.println(":((---");
//								System.out.println(Thread.currentThread().getStackTrace());
//								System.exit(-1);
						}
					}
				}
			}while(!issueQueue.isEmpty() && simTime==issueQueue.peek().getDelay());
		}
		if (RuntimeConfig.VERBOSE)
			outputFile.println();	//adding a new line at the end of the trace		
		return simTime;
	}



	public void BasicScheduling(Scheduling type, DirectedGraph<Vertex, DefaultEdge> DFG){
		ArrayList<ArrayList<Vertex>> result=new ArrayList<ArrayList <Vertex>>();
		ArrayList<Vertex> temp=new ArrayList<Vertex>();

		Vertex cur0 = null, cur1;
		
		//Initialize graph for traversing
		for (Iterator<Vertex> iterator = DFG.vertexSet().iterator(); iterator.hasNext();) {
			cur0=iterator.next();
			cur0.setLevel(-1);
			if (cur0.isSentinel()){
				if ((type == Scheduling.ASAP && cur0.getName().compareToIgnoreCase("start")==0)||
						(type == Scheduling.ALAP && cur0.getName().compareToIgnoreCase("end")==0)){
					temp.add(cur0);
					cur0.setLevel(0);
				}
			}
		}
		
		result.add(new ArrayList<Vertex>());
		if (type==Scheduling.ASAP)
			for (Iterator<DefaultEdge> iterator = DFG.outgoingEdgesOf(temp.get(0)).iterator(); iterator.hasNext();) {
				cur0=DFG.getEdgeTarget(iterator.next());
				cur0.setLevel(0);
				result.get(0).add(cur0);
				temp.add(cur0);
			}
		else
			for (Iterator<DefaultEdge> iterator = DFG.incomingEdgesOf(temp.get(0)).iterator(); iterator.hasNext();) {
				cur0=DFG.getEdgeSource(iterator.next());
				cur0.setLevel(0);
				result.get(0).add(cur0);
				temp.add(cur0);
			}

		//Removing the sentinel
		temp.remove(0);
		while(!temp.isEmpty()){
			cur0=temp.remove(0);
			if (type==Scheduling.ASAP)
				for (Iterator<DefaultEdge> iterator0 = DFG.outgoingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
					cur1=DFG.getEdgeTarget(iterator0.next());
					if (cur1.isSentinel())
						continue;
					cur1.incReadyInpts();
					if (cur1.getLevel()==-1 && cur1.getReadyInpts() == DFG.incomingEdgesOf(cur1).size()){
						cur1.setLevel(cur0.getLevel()+1);
						temp.add(cur1);
						if (result.size()<=cur1.getLevel())
							result.add(new ArrayList<Vertex>());
						result.get(cur1.getLevel()).add(cur1);
					}				
				}
			else
				for (Iterator<DefaultEdge> iterator0 = DFG.incomingEdgesOf(cur0).iterator(); iterator0.hasNext();) {
					cur1=DFG.getEdgeSource(iterator0.next());
					if (cur1.isSentinel())
						continue;
					cur1.incReadyInpts();
					if (cur1.getLevel()==-1 && cur1.getReadyInpts() == DFG.outgoingEdgesOf(cur1).size()){
						cur1.setLevel(cur0.getLevel()+1);
						temp.add(cur1);
						if (result.size()<=cur1.getLevel())
							result.add(0,new ArrayList<Vertex>());
						result.get(result.size()-cur1.getLevel()-1).add(cur1);
					}				
				}

		}
		
		if (RuntimeConfig.VERBOSE)
			System.out.println("Scheduling is completed succesfully!");
		
		if (RuntimeConfig.DEBUG){
			System.out.println("Scheduling result:");
			for (int i = 0; i < result.size(); i++) {
				System.out.print("Level "+i+":\t");
				for (int j = 0; j < result.get(i).size(); j++) {
					System.out.print("{"+result.get(i).get(j)+"} ");
				}
				System.out.println();
			}
		}
		
	}

}
