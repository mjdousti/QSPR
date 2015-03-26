/*
 * 
 * Copyright (C) 2014 Mohammad Javad Dousti and Massoud Pedram, University of Southern California.
 * All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qspr.layout;

import org.jgrapht.graph.DefaultWeightedEdge;

@SuppressWarnings("serial")
public class ChannelEdge extends DefaultWeightedEdge {
    private Channel channel=null;
    private int turnDelay;
	private Junction v1;
    private Junction v2;


    public ChannelEdge(Junction j1, Junction j2, Channel c) {
        channel=c;
        channel.setV1(j1);
        channel.setV2(j2);
    }
    
    public ChannelEdge(Junction j1, Junction j2, int turnDelay) {
    	v1=j1;
        v2=j2;
        this.turnDelay=turnDelay;
    }

    public boolean isUsed(){
    	return channel.isUsed();
    }
    
    public void setUsed(boolean status){
    	channel.setUsed(status);
    }

    public Junction getV1() {
        if (channel==null)
        	return v1;
        else       			
        	return channel.getV1();
    }

    public Junction getV2() {
        if (channel==null)
        	return v2;
        else       			
        	return channel.getV2();
    }
    
    public Junction getOtherVertex(Junction v) {
        if (v==channel.getV1())
        	return channel.getV2();
        else if(v==channel.getV2())
        	return channel.getV1();
        else
        	return null;
    }
    
    public void replaceVertex(Junction oldJunc, Junction newJunc){
    	if (channel!=null && channel.getV1()==oldJunc)
    		channel.setV1(newJunc);
    	else if (channel==null && channel.getV1()==oldJunc)
    		v1=newJunc;
    	else if (channel!=null && channel.getV2()==oldJunc)
    		channel.setV2(newJunc);
    	else
    		v2=newJunc;
    }
    
    public Channel getChannel(){
    	return channel;
    }
    
    public int incWeight(){
    	if (channel==null)
    		return turnDelay;
    	else
    		return channel.incWeight();
    }
    
    public int decWeight(){
    	if (channel==null)
    		return turnDelay;
    	else
    		return channel.decWeight();
    }
    
    public double getWeight(){
    	if (channel==null)
    		return turnDelay;
    	else
    		return channel.getWeight();
    }
    
    public String toString(){
    	String output=new String();
    	output+="("+getV2().getPosition().height+","+getV2().getPosition().width+") -> "
		+"("+ getV1().getPosition().height+","+getV1().getPosition().width+")";
    	
    	return output;
    }
}
