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
import java.util.Comparator;

public class NearestSquare implements Comparator<Trap>{
	private Dimension a;
	
	public int compare(Trap o1, Trap o2) {
		int d1=Math.abs(a.height-o1.getPosition().height)+Math.abs(a.width-o1.getPosition().width);
		int d2=Math.abs(a.height-o2.getPosition().height)+Math.abs(a.width-o2.getPosition().width);
		
		if (d1<d2)
			return -1;
		else if (d1>d2)
			return 1;
		 //just to get unique answer
		else if(o1.getPosition().height<o2.getPosition().height)
			return -1;
		else if(o1.getPosition().height>o2.getPosition().height)
			return 1;
		else if(o1.getPosition().width<o2.getPosition().width)
			return -1;
		else if(o1.getPosition().width>o2.getPosition().width)
			return 1;
		else
			return 0;
		
	}
	
	public NearestSquare(Dimension x) {
		a=x;
	}

}
