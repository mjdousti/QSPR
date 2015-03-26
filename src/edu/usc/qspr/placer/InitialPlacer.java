/*
 * 
 * Copyright (C) 2014 Mohammad Javad Dousti and Massoud Pedram, University of Southern California.
 * All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qspr.placer;

import java.awt.Dimension;
import java.util.Random;

import edu.usc.qspr.layout.Layout;
import edu.usc.qspr.qasm.QASM;


public class InitialPlacer {
	private static QASM qasm;
	private static Layout layout;
	private static Random randomGenerator;
	
	
	public static enum Heuristic{
		Center, Random, ShuffledCenter
	}
	
	public static void InitialPlacer(QASM q, Layout l, Heuristic heuristic) {
		randomGenerator=new Random();
		layout=l;
		qasm=q;
		assignQubits(heuristic);
	}
	
	private static void assignQubits (Heuristic heuristic){
		Dimension place=new Dimension();
		Dimension size=layout.getLayoutSize();
		
		String[] qubits=qasm.getQubitList();
		if (qubits.length>layout.getMaxAllowedQubits()){
			System.err.println("# of qubits in QASM file is more than the amounts allowed by the fabric. You sould use a larger fabric");
			System.exit(-1);
		}
		switch (heuristic){
		case Center:
			place.height = size.height/2;
			place.width  = size.width/2;
			layout.sortTraps(place, false,  qasm.getQubitList().length);
			for (int i = 0; i < qubits.length; i++) {
				layout.assignNewQubit(qubits[i], layout.getNearestFreeTrap(true).getPosition());
			}		
			break;
		case ShuffledCenter:
			place.height = size.height/2;
			place.width  = size.width/2;
			layout.sortTraps(place, true, qasm.getQubitList().length);
			for (int i = 0; i < qubits.length; i++) {
				layout.assignNewQubit(qubits[i], layout.getNearestFreeTrap(true).getPosition());
			}		
			break;
		case Random:
			for (int i = 0; i < qubits.length; i++) {
				place.width=randomGenerator.nextInt(size.width);
				place.height=randomGenerator.nextInt(size.height);
				layout.sortTraps(place, false,  qasm.getQubitList().length);
				layout.assignNewQubit(qubits[i], layout.getNearestFreeTrap(true).getPosition());
			}		
			break;
		}
	}
}
