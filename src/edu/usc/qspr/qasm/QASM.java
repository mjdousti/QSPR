/*
 * 
 * Copyright (C) 2014 Mohammad Javad Dousti and Massoud Pedram, University of Southern California.
 * All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qspr.qasm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class QASM {
	private Map<String, Integer> dependencyList=new Hashtable<String, Integer>();
	private int commandNo=0;
	List<Vertex> commands=new ArrayList<Vertex>();
	private DirectedGraph<Vertex, DefaultEdge> graph;

	public QASM(){
		graph = new SimpleDirectedGraph<Vertex, DefaultEdge>(DefaultEdge.class);

		//adding "start" node in graph
		commands.add(new Vertex("start", commandNo, (String[]) null));
		graph.addVertex(commands.get(commandNo));

		//adding "end" node in graph
		commands.add(new Vertex("end", commandNo+1, (String[]) null));
		graph.addVertex(commands.get(commandNo+1));

		//adding a direct edge from "start" node to "end" node
		graph.addEdge(commands.get(0), commands.get(1));

	}

	public void reverseCommandsOrder(){
		Collections.reverse(commands);
		for (int i = 0; i < commands.size(); i++) {
			commands.get(i).setCommandNo(i);
		}
	}
	
	public DirectedGraph<Vertex, DefaultEdge> getDFG(){

		return graph;
	}
	
	public List<Vertex> getCommandsList(){
		return commands;		
	}
	
	public String[] getQubitList(){
		String[] qubits=new String[dependencyList.size()];
		Iterator<Entry<String, Integer>> it=dependencyList.entrySet().iterator();
		int i=0;
		while (it.hasNext()){
			qubits[i]=it.next().getKey();
			i++;
		}
		return qubits;
	}
	
	public void printDFG(){
		for (DefaultEdge e : graph.edgeSet()) {
			System.out.println(e.toString());                    
		}
	}
	
	public void printCommands(){
		for (int i = 0; i < commands.size(); i++) {
			System.out.println(commands.get(i));
		}
	}
	
	public void printDependancyList(){
		for (Map.Entry<String, Integer> entry : dependencyList.entrySet())
		{
			System.out.println(entry.getKey());
		}
	}

	/****************************************************************************/
	// For helping parser
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> createArrayList(T ... elements) { 
		ArrayList<T> list = new ArrayList<T>();  
		for (T element : elements) { 
			list.add(element); 
		} 
		return list; 
	} 

	private void parseError(String token){
		System.err.println("Qubit `"+token+"` is not defined.");
		//TODO: convert to an exception with correct message
		System.exit(-1);
	}

	public void incCommandNo(){
		commandNo++;
	}

	public void defineQubit(String q){
		//		System.out.println(q);
		//Qubit definition
		//Adding the qubits in dependencyList
		if (dependencyList.containsKey(q)==true){
			System.err.println("Qubit "+q+" is already defined.");
			System.exit(-1);
		}
		dependencyList.put(q, new Integer (0));
		//To access the value of qubit
		//if (qubitValue!=null)
		//	System.out.println(qubitValue.image);
	}

	//Shifting "end" one place ahead in commands list
	public void shiftEnd(){
		commands.add(commands.get(commandNo).setCommandNo(commandNo+1));
	}

	public void addOneOpInst(String cmd, String op){
		//Reports error if the used qubit is not defined before
		if (dependencyList.containsKey(op)==false){
			parseError(op);
		}
		String[] temp={op};
		//Vertex(String c, int no, int p, Qubit ...ops)
		commands.set(commandNo, new Vertex (cmd, commandNo, temp));
		//Adding new command in the graph
		graph.addVertex(commands.get(commandNo));
		graph.addEdge(commands.get(0), commands.get(commandNo));

		//Adding an edge to the node which depends on 
		if (dependencyList.get(op).intValue()>0){
			graph.addEdge(commands.get(dependencyList.get(op).intValue()), commands.get(commandNo));

			//Remove edge to the "start" node
			if (graph.containsEdge(commands.get(0), commands.get(commandNo)))
				graph.removeEdge(commands.get(0), commands.get(commandNo));
		}
		//Changing the dependency of its operand to point to itself
		dependencyList.put(op, new Integer (commandNo));	
	}

	public void addTwoOpInst(String cmd, String op0, String op1){
		if (op0.equals(op1)){
			System.err.println("Error: operands of a 2-qubit operator, i.e. `"+op0+"`, cannot be the same");
			//TODO: convert to an exception with correct message
			System.exit(-1);
		}
		//Reports error if the used qubits are not defined before
		if (dependencyList.containsKey(op0)==false){
			parseError(op0);
		} else if (dependencyList.containsKey(op1)==false){
			parseError(op1);
		}

		//Adding the command in commands list
		//		vertex=cmd.image+" "+q0.image+","+q1.image;
		//Vertex(String c, int no, int p, Qubit ...ops)
		String[] temp={op0, op1};
		commands.set(commandNo, new Vertex (cmd,commandNo, temp));

		//Adding new command in the graph
		graph.addVertex(commands.get(commandNo));
		graph.addEdge(commands.get(0), commands.get(commandNo));

		//Adding an edge to the node which depends on 
		if (dependencyList.get(op0).intValue()>0){
			graph.addEdge(commands.get(dependencyList.get(op0).intValue()), commands.get(commandNo));
			//Remove edge to the "start" node
			if (graph.containsEdge(commands.get(0), commands.get(commandNo)))
				graph.removeEdge(commands.get(0), commands.get(commandNo));
		}
		//It is assumed that the target qubit does not make any dependency on another target (wrong assumption?!)
		//Also, plz note that the schedular may encounter problems if this section is uncommented
		//Because it is assuming that any node has only one parent 
		if (dependencyList.get(op1).intValue()>0){
			graph.addEdge(commands.get(dependencyList.get(op1).intValue()), commands.get(commandNo));
			//Remove edge to the "start" node
			if (graph.containsEdge(commands.get(0), commands.get(commandNo)))
				graph.removeEdge(commands.get(0), commands.get(commandNo));
		}

		//Changing the dependency of its operand to point to itself
//		dependencyList.put(op1, new Integer (commandNo));
		
		//NEW
		dependencyList.put(op0, new Integer (commandNo));
	}	


	//Common operation after adding 1-2 -qubit operators
	public void operation(){
		//add an edge to the "end" sentinel
		graph.addEdge(commands.get(commandNo), commands.get(commandNo+1));

		//removing any common edges between the "end" and the parent of newly added node
		List<DefaultEdge> l=new LinkedList<DefaultEdge>();
		for (DefaultEdge v : graph.incomingEdgesOf(commands.get(commandNo+1))) {
			Vertex vv=graph.getEdgeSource(v);
			if (vv!=commands.get(commandNo) && graph.containsEdge (vv,commands.get(commandNo)))
				//g1.removeEdge(v);
				l.add(v);
		}
		//This for is added to avoid strange problems while removing edges dynamically!
		for (DefaultEdge defaultEdge : l) {
			graph.removeEdge(defaultEdge);
		}

	}

}
