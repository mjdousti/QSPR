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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.SimpleWeightedGraph;

import edu.usc.qspr.RuntimeConfig;
import edu.usc.qspr.layout.Junction.Direction;


public class Layout {
	private ArrayList<ArrayList<Trap>> explored=new ArrayList<ArrayList<Trap>>();
	
	private Dimension dim;
	private Square [][][]fabric;
	private Map<String, Operation>supportedOps=new HashMap<String, Operation>();
	//	private Qubit[] qubits;
	//May it's better to use a MiniMap
	private Map<String, Qubit> qubits=new HashMap<String, Qubit>();
	private LinkedList<Trap> trapEmptyList=new LinkedList<Trap>();
	private int qubitsNo;
	private SimpleWeightedGraph<Junction, ChannelEdge> layoutGraph;
//	private int availableLasers=RuntimeConfig.LASERS;
	
	private double moveErrorRate;
	private int moveDelay;
	public enum Types{
		Channel, Trap, Junction
	}
	

//	public int getAvailableLasersNo(){
//		return availableLasers;
//	}
//	
//	public void decAvailableLasersNo(){
//		availableLasers--;
//	}
//	public void incAvailableLasersNo(){
//		availableLasers++;
//	}
	
	public void printQubitPlaces(){
		Set<Entry<String, Qubit>> qSet =qubits.entrySet();
		for (Iterator<Entry<String, Qubit>> iterator = qSet.iterator(); iterator.hasNext();) {
			Entry<String, Qubit> entry = iterator.next();
			System.out.println(entry.getKey()+" "+entry.getValue().getPosition().height+"x"+entry.getValue().getPosition().width);
			
		}
	}
	
	
	public Channel getNearestChannel(int height, int width){
		if (fabric[height-1][width][0]!=null && fabric[height-1][width][0].getSquareType()==Types.Channel)
			height--;
		else if (fabric[height+1][width][0]!=null && fabric[height+1][width][0].getSquareType()==Types.Channel)
			height++;
		else if (fabric[height][width-1][0]!=null && fabric[height][width-1][0].getSquareType()==Types.Channel)
			width--;
		else if (fabric[height][width+1][0]!=null && fabric[height][width+1][0].getSquareType()==Types.Channel)
			width++;
		
		return (Channel)fabric[height][width][0];
	}
	
	public void free(Dimension x, Qubit qubit) {
		getSquare(x).removeQubit(qubit);
		if (!getSquare(x).isOccupied() && getSquareType(x)==Types.Trap)
			trapEmptyList.add((Trap)getSquare(x));			
	}

	
	public void occupy(Dimension x, Qubit q){
		getSquare(x).addQubits(q);
	}
	
	public Channel getNearestChannel(Dimension a){
		return getNearestChannel(a.height, a.width);	
	}	
	
	public Square getNearestJunction(Dimension x){
		return getNearestJunction(x.height, x.width);
	}	
	
	public Square getNearestJunction(int height, int width){
		Dimension temp;
		switch (fabric[height][width][0].getSquareType()){
		case Junction:
			return fabric[height][width][1];
		case Trap:	//Finds the nearest channel and use the same method for the nearest neighbour for channel. 
			if (fabric[height-1][width][0]!=null && fabric[height-1][width][0].getSquareType()==Types.Channel)
				height--;
			else if (fabric[height+1][width][0]!=null && fabric[height+1][width][0].getSquareType()==Types.Channel)
				height++;
			else if (fabric[height][width-1][0]!=null && fabric[height][width-1][0].getSquareType()==Types.Channel)
				width--;
			else if (fabric[height][width+1][0]!=null && fabric[height][width+1][0].getSquareType()==Types.Channel)
				width++;
		case Channel:
			if (distance(((Channel)fabric[height][width][0]).getLeftBorder(), new Dimension(width, height)) <= distance(((Channel)fabric[height][width][0]).getRightBorder(), new Dimension(width, height))){
				temp=new Dimension(((Channel)fabric[height][width][0]).getLeftBorder());
				if (((Channel)fabric[height][width][0]).isHorizontal())
					temp.width--;
				else
					temp.height--;
//				return getSquare(temp);
				if (((Channel)fabric[height][width][0]).isHorizontal())
					return fabric[temp.height][temp.width][1];
				else
					return fabric[temp.height][temp.width][2];
			}else{
				temp=new Dimension(((Channel)fabric[height][width][0]).getRightBorder());
				if (((Channel)fabric[height][width][0]).isHorizontal())
					temp.width++;
				else
					temp.height++;
//				return getSquare(temp);
				if (((Channel)fabric[height][width][0]).isHorizontal())
					return fabric[temp.height][temp.width][1];
				else
					return fabric[temp.height][temp.width][2];
			}
		default:
			return null;

		}
	}
	
	public boolean repeated(ArrayList<Trap> shuffleList){
		for(int i=0;i<explored.size();i++){
			for(int j=0;j<shuffleList.size();j++){
				if (!shuffleList.get(j).getPosition().equals(explored.get(i).get(j).getPosition())){
					continue;
				}
				else if (j==shuffleList.size()-1){
					return true;
				}
			}
		}
		return false;
	}
	
	public void printExploredPaths(){
		for (int i = 0; i < explored.size(); i++) {
			for (int j = 0; j < explored.get(i).size(); j++) {
				System.out.print(explored.get(i).get(j).getPosition().height+"x"+explored.get(i).get(j).getPosition().width+" ");
			}
			System.out.println();

		}
	}
	
	public void sortTraps( Dimension a, boolean shuffle, int qubitCount){
		Collections.sort(trapEmptyList, new NearestSquare(a));
		if (shuffle){
			ArrayList<Trap> shuffleList=new ArrayList<Trap>();
			for (int i=0;i<qubitCount;i++){
				shuffleList.add(trapEmptyList.remove());
			}
			//do{
				Collections.shuffle(shuffleList, new Random(System.nanoTime()));
			//}while(repeated(shuffleList));
			//explored.add(shuffleList);
			for (int i=0;i<qubitCount;i++){
				trapEmptyList.add(i, shuffleList.get(i));
			}
		}
	}
	
	public Trap getNearestFreeTrap(Dimension a, boolean assign){
		if (trapEmptyList.size()==0){
			System.out.println("BICHARE SHODI!!");
			System.exit(-1);
		}
		Collections.sort(trapEmptyList, new NearestSquare(a));
		if (assign)
			return trapEmptyList.remove();
		else
			return trapEmptyList.getFirst();
	}

	
	public Trap getNearestFreeTrap(boolean assign){
		if (trapEmptyList.size()==0){
			System.out.println("BICHARE SHODI!!!!");
			System.exit(-1);
		}
		
		if (assign)
			return trapEmptyList.remove();
		else
			return trapEmptyList.getFirst();
	}
	
	public static int distance (Dimension p, Dimension q){
		if (p.height==q.height)
			return Math.abs(p.width-q.width);
		else
			return Math.abs(p.height-q.height);
	}

	
	

	public void setMoveInfo(double errorRate, int delay){
		moveErrorRate=errorRate;
		moveDelay=delay;
	}
	
	public double getMoveErrorRate(){
		return moveErrorRate;
	}
	
	public int getMoveDelay(){
		return moveDelay;
	}
	


	public Layout(Dimension d, int qc){
		qubitsNo=qc;

		dim=new Dimension(d);
		fabric=new Square[dim.height][dim.width][3];

		initFabric();
	}

	public int getMaxAllowedQubits(){
		return qubitsNo;
	}
	
	public boolean assignNewQubit(String name, Dimension initPos){
		if (qubits.size()==qubitsNo)
			return false; //all qubits are already assigned

		if (RuntimeConfig.DEBUG)
			System.out.println("Qubit "+name+" is assigned "+initPos.height+"x"+initPos.width+".");
		
		Qubit temp=new Qubit(name, initPos, this);
		qubits.put(name, temp);

		//Just to be extra cautious
		if (isTrap(initPos.height, initPos.width)){
			occupy(initPos, temp);
		}
		
		return true;
	}
	
	public Qubit getQubit(String s){
		return qubits.get(s);		
	}
	

	
	public void addNewOperation(Operation op){
		supportedOps.put(op.getName().toUpperCase(), op);
	}
	
	public int getOpDelay(String s){
		return supportedOps.get(s.toUpperCase()).getDelay();
	}
	
	public Operation returnOperation(String s){
		return supportedOps.get(s.toUpperCase());
	}
	
	public boolean isOperationSupported(String s){
		return supportedOps.containsKey(s.toUpperCase());
	}


	public void addJunction(int height, int width){
		fabric[height][width][0]=new Junction(new Dimension(width, height), new Dimension(width, height), 1, Types.Junction, Direction.Old);
	}

	public Types getSquareType(int height, int width){
		return fabric[height][width][0].getSquareType();
	}
	
	public Types getSquareType(Dimension a){
		return getSquareType(a.height, a.width);
	}
	
	public Square getSquare(Dimension d){
		return fabric[d.height][d.width][0];
	}
	
	public Square getSquare(int height, int width){
		return fabric[height][width][0];
	}

	
	public Dimension getLayoutSize(){
		//returns a new instance of Dimension to keep data of layout safe
		return new Dimension(dim);
	}

	public boolean isTrap(int height, int width){
		return fabric[height][width][0].getSquareType()==Types.Trap ? true  : false;
	}

	public boolean isTrap(Dimension a){
		return isTrap(a.height, a.width);
	}

	public boolean isChannel(int height, int width){
		return fabric[height][width][0].getSquareType()==Types.Channel ? true  : false;
	}


	public boolean isChannel(Dimension x){
		return isChannel(x.height, x.width);
	}
	
	
	
	public boolean isJunction(int height, int width){
		if (fabric[height][width][0]==null)
			return false;
		else
			return fabric[height][width][0].getSquareType()==Types.Junction ? true  : false;
	}

	
	public boolean isJunction(Dimension x){
		return isJunction(x.height, x.width);
	}

	public void addSquares(Types sq, int h0, int w0, int h1, int w1, int step){
		for (int h = h0; h <= h1; h+=step) {
			for (int w = w0; w <= w1; w+=step) {
				if (w>0 && fabric[h][w-1][0]!=null && fabric[h][w-1][0].getSquareType()==sq){
					fabric[h][w][0]=fabric[h][w-1][0];
				}else if (h>0 && fabric[h-1][w][0]!=null && fabric[h-1][w][0].getSquareType()==sq){
					fabric[h][w][0]=fabric[h-1][w][0];
				}else{
					if (sq==Types.Channel)
						//Vertical
						if (fabric[h-1][w][0]!=null && fabric[h-1][w][0].getSquareType()==Types.Junction)
							fabric[h][w][0]=new Channel(new Dimension(w0, h0), new Dimension(w1, h1), step, sq, false);
						else//Horizontal
							fabric[h][w][0]=new Channel(new Dimension(w0, h0), new Dimension(w1, h1), step, sq, true);
					else{//TRAP
						//new Dimension(width, height)
						if (w-1>=0 && fabric[h][w-1][0]!=null && fabric[h][w-1][0].getSquareType()==Types.Channel)
							fabric[h][w][0]=new Trap(new Dimension(w, h), new Dimension(w, h), 1, sq, Qubit.Direction.Left);
						else if (h-1>=0 && fabric[h-1][w][0]!=null && fabric[h-1][w][0].getSquareType()==Types.Channel)
							fabric[h][w][0]=new Trap(new Dimension(w, h), new Dimension(w, h), 1, sq, Qubit.Direction.Up);
						else if (w+1 < dim.width && fabric[h][w+1][0]!=null && fabric[h][w+1][0].getSquareType()==Types.Channel){
							fabric[h][w][0]=new Trap(new Dimension(w, h), new Dimension(w, h), 1, sq, Qubit.Direction.Right);
						}else if (h+1 < dim.height && fabric[h+1][w][0]!=null && fabric[h+1][w][0].getSquareType()==Types.Channel){
							fabric[h][w][0]=new Trap(new Dimension(w, h), new Dimension(w, h), 1, sq, Qubit.Direction.Down);
						}else{
							fabric[h][w][0]=new Trap(new Dimension(w, h), new Dimension(w, h), 1, sq, null);
						}
						trapEmptyList.add((Trap) fabric[h][w][0]);
					}
				}
			}
		}
	}
	
	public SimpleWeightedGraph<Junction, ChannelEdge> getGraph(){
		return layoutGraph;
	}
	
	public void fixFabricSquares(){
		if (RuntimeConfig.DEBUG)
			System.out.println("# of traps on the fabric: "+trapEmptyList.size());
		int k;
		for (int h = 0; h < dim.height; h++) {
			for (int w = 0; w < dim.width; w++) {
				if (fabric[h][w][0]!=null && fabric[h][w][0].getSquareType()==Types.Channel){
					if (w-1>=0 && fabric[h][w-1][0]!=null && fabric[h][w-1][0].getSquareType()==Types.Channel)
						fabric[h][w]=fabric[h][w-1];
					else if (h-1>=0 && fabric[h-1][w][0]!=null && fabric[h-1][w][0].getSquareType()==Types.Channel)
						fabric[h][w]=fabric[h-1][w];
					else if (w+1 < dim.width && fabric[h][w+1][0]!=null && fabric[h][w+1][0].getSquareType()==Types.Channel){
						k=1;
						while (fabric[h][w+k][0].getSquareType()==Types.Channel)
							k++;
						
						if (fabric[h-1][w][0]!=null && fabric[h-1][w][0].getSquareType()==Types.Junction)
							fabric[h][w][0]=new Channel(new Dimension(w, h), new Dimension(w+k-1, h), 1, Types.Channel, false);
						else//Horizontal
							fabric[h][w][0]=new Channel(new Dimension(w, h), new Dimension(w+k-1, h), 1, Types.Channel, true);
					}else if (h+1 < dim.height && fabric[h+1][w][0]!=null && fabric[h+1][w][0].getSquareType()==Types.Channel){
						k=1;
						while (fabric[h+k][w][0].getSquareType()==Types.Channel)
							k++;
						if (fabric[h-1][w][0]!=null && fabric[h-1][w][0].getSquareType()==Types.Junction)
							fabric[h][w][0]=new Channel(new Dimension(w, h), new Dimension(w, h+k-1), 1, Types.Channel, false);
						else//Horizontal
							fabric[h][w][0]=new Channel(new Dimension(w, h), new Dimension(w, h+k-1), 1, Types.Channel, true);
						
					}else{//Single channel
						if (fabric[h-1][w][0]!=null && fabric[h-1][w][0].getSquareType()==Types.Junction)
							fabric[h][w][0]=new Channel(new Dimension(w, h), new Dimension(w, h), 1, Types.Channel, false);
						else//Horizontal
							fabric[h][w][0]=new Channel(new Dimension(w, h), new Dimension(w, h), 1, Types.Channel, true);
					}
						
				}
			}
		}
	}
	
	public void makeGraph(){
		layoutGraph = 
				new SimpleWeightedGraph<Junction, ChannelEdge>(new ClassBasedEdgeFactory<Junction, ChannelEdge>(ChannelEdge.class)); 
        
		Square prev;
		ChannelEdge channelEdge;
		Junction v1, v2;
		for (int h = 0; h < dim.height; h++) {
			prev=null;
			for (int w = 0; w < dim.width; w++) {
				if (fabric[h][w][0]==null || prev==fabric[h][w][0]){
					continue;
				}else if (fabric[h][w][0].getSquareType()==Types.Junction){
					layoutGraph.addVertex((Junction)fabric[h][w][0]);
					//Look for any channel on the left
					if (w>0 && fabric[h][w-1][0]!=null && fabric[h][w-1][0].getSquareType()==Types.Channel){
						//Just to be extra cautious not to guess that on the left of every channel is a junction 
						if (((Channel)fabric[h][w-1][0]).getLeftBorder().width-1>=0 &&
							fabric[h][((Channel)fabric[h][w-1][0]).getLeftBorder().width-1][0].getSquareType() == Types.Junction){
							v1=(Junction)fabric[h][w][0];
							v2=(Junction)fabric[h][((Channel)fabric[h][w-1][0]).getLeftBorder().width-1][0];
							channelEdge=new ChannelEdge(v1, v2, (Channel) fabric[h][w-1][0]);
							layoutGraph.addEdge(v1, v2, channelEdge);
//							layoutGraph.setEdgeWeight(channelEdge, channelEdge.getChannel().getBaseCost());
						}
					}
					
					//Look for any junction on the above
					if (h>0 && fabric[h-1][w][0]!=null && fabric[h-1][w][0].getSquareType()==Types.Channel){
						//Just to be extra cautious not to guess that on the above of every channel is a junction 
						if (((Channel)fabric[h-1][w][0]).getLeftBorder().height-1>=0 &&
							fabric[((Channel)fabric[h-1][w][0]).getLeftBorder().height-1][w][0].getSquareType() == Types.Junction){
							v1=(Junction)fabric[h][w][0];
							v2= (Junction)fabric[((Channel)fabric[h-1][w][0]).getLeftBorder().height-1][w][0];
							channelEdge=new ChannelEdge(v1, v2, (Channel)fabric[h-1][w][0]);
							layoutGraph.addEdge(v1, v2, channelEdge);
//							layoutGraph.setEdgeWeight(channelEdge, channelEdge.getChannel().getBaseCost());
						}
					}
				}
				prev=fabric[h][w][0];
			}
		}
		fixGraph();
	}
	
	private void fixGraph(){
		Junction junction, hVertex, vVertex;
		Junction temp;
		ChannelEdge cEdge;
		Dimension d;
		
		Object []junctions=layoutGraph.vertexSet().toArray();
		Object []edges;
		ChannelEdge edge;
		for (int i = 0; i < junctions.length; i++) {
			junction=(Junction) junctions[i];
			d=junction.getPosition();
			
			hVertex=new Junction(junction, Direction.Horizontal);
			vVertex=new Junction(junction, Direction.Vertical);
			layoutGraph.addVertex(hVertex);
			layoutGraph.addVertex(vVertex);
			fabric[d.height][d.width][1]=hVertex;
			fabric[d.height][d.width][2]=vVertex;
			
			cEdge=new ChannelEdge(hVertex, vVertex, this.getOpDelay("Turn"));
			layoutGraph.addEdge(hVertex, vVertex, cEdge);			
			layoutGraph.setEdgeWeight(cEdge, this.getOpDelay("Turn"));						

			
			//get all edges
			edges=layoutGraph.edgesOf(junction).toArray();
			for (int j = 0; j < edges.length; j++) {
				edge=(ChannelEdge)edges[j];
				if (edge.getChannel().isHorizontal()){
					temp=edge.getOtherVertex(junction);
					layoutGraph.removeEdge(edge);
					edge.replaceVertex(junction, hVertex);
					layoutGraph.addEdge(hVertex, temp, edge);
					layoutGraph.setEdgeWeight(edge, edge.getChannel().getBaseCost());
					
				}else{
					temp=edge.getOtherVertex(junction);
					layoutGraph.removeEdge(edge);
					edge.replaceVertex(junction, vVertex);
					layoutGraph.addEdge(vVertex, temp, edge);
					layoutGraph.setEdgeWeight(edge, edge.getChannel().getBaseCost());
				}
			}
			layoutGraph.removeVertex(junction);

		}
		
	}

	void initFabric(){
		for (int i = 0; i < dim.height; i++) {
			for (int j = 0; j < dim.width; j++) {
				for (int k = 0; k < 3; k++) {
					fabric[i][j][k]=null;
				}
			}
		}
	}

	public void printFabric(){
		System.out.println("Fabric "+dim.height+","+dim.width+":");
		for (int i = 0; i < dim.height; i++) {
			for (int j = 0; j < dim.width; j++) {
				if (fabric[i][j][0]==null)
					System.out.print("*");
				else {
					switch(fabric[i][j][0].getSquareType())
					{
					case Channel:
						System.out.print("C");
						break;
					case Trap:
						System.out.print("T");
						break;
					case Junction:
						System.out.print("J");
						break;
					}
				}
			}
			System.out.println();
		}
	}


	public void assignLastTrap(Dimension x) {
		if (!trapEmptyList.remove().getPosition().equals(x)){
			System.out.println("ERRRR");
			System.exit(-1);
		}
	}

	public void usageStatistics(){
		double channelUtilization=0;
		int channelCount=0;
		
		double jucntionUtilization=0;
		int junctionCount=0;

		double trapUtilization=0;
		int trapCount=0;

		ChannelEdge c;
		for (Iterator<ChannelEdge> iterator = layoutGraph.edgeSet().iterator(); iterator.hasNext();) {
			c=iterator.next();
			if (c.getChannel()!=null){
				channelCount++;
				if (c.isUsed())
					channelUtilization++;
			}
		}


		for (int i = 0; i < dim.height; i++) {
			for (int j = 0; j < dim.width; j++) {
				if (fabric[i][j][0]==null)
					continue;
				if (isJunction(i, j)){
					junctionCount++;
					if (getSquare(i, j).isUsed())
						jucntionUtilization++;
				}else if (isTrap(i, j)){
					trapCount++;
					if (getSquare(i, j).isUsed())
						trapUtilization++;
				}
			}
		}

		System.out.printf("Trap Utilization: %.2f%%\n",trapUtilization/trapCount*100);
		System.out.printf("Channel Utilization: %.2f%%\n",channelUtilization/channelCount*100);
		System.out.printf("Junction Utilization: %.2f%%\n",jucntionUtilization/junctionCount*100);

	}
	
	
	//retains all the traps to the empty trap list
	public void clean(){
		//Clearing the usage statistics for channels
		ChannelEdge c;
		for (Iterator<ChannelEdge> iterator = layoutGraph.edgeSet().iterator(); iterator.hasNext();) {
			c=iterator.next();
			if (c.getChannel()!=null){
				c.setUsed(false);
			}
		}

		//Clearing the usage statistics for junctions and traps
		for (int i = 0; i < dim.height; i++) {
			for (int j = 0; j < dim.width; j++) {
				if (fabric[i][j][0]==null)
					continue;
				else if (isJunction(i, j)){
					getSquare(i, j).setUsed(false);
				}else if (isTrap(i,j)){
					if (!trapEmptyList.contains((Trap)getSquare(i,j))){
						trapEmptyList.add((Trap)getSquare(i,j));
					}
					getSquare(i, j).setUsed(false);
				}
			}
		}
		qubits.clear();
	}
	
	//retains all the traps to the empty trap list but the ones which are occupied
	public void clear(){
		//Clearing the usage statistics for channels
		ChannelEdge c;
		for (Iterator<ChannelEdge> iterator = layoutGraph.edgeSet().iterator(); iterator.hasNext();) {
			c=iterator.next();
			if (c.getChannel()!=null){
				c.setUsed(false);
			}
		}

		//Clearing the usage statistics for junctions and traps
		for (int i = 0; i < dim.height; i++) {
			for (int j = 0; j < dim.width; j++) {
				if (fabric[i][j][0]==null)
					continue;
				else if (isJunction(i, j)){
					getSquare(i, j).setUsed(false);
				}else if (isTrap(i,j)){
					if (!getSquare(i, j).isOccupied()){
						if (!trapEmptyList.contains((Trap)getSquare(i,j))){
							trapEmptyList.add((Trap)getSquare(i,j));
						}
						getSquare(i, j).setUsed(false);
					}
				}
			}
		}
		
		
		
	}

	public int getTrapEmptyListSize(){
		return trapEmptyList.size();
	}
	
}
