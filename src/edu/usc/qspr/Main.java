/*
 * 
 * Copyright (C) 2014 Mohammad Javad Dousti and Massoud Pedram, University of Southern California.
 * All rights reserved.
 * 
 * Please refer to the LICENSE file for terms of use.
 * 
*/
package edu.usc.qspr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.usc.qspr.layout.Layout;
import edu.usc.qspr.parser.layout.LayoutParser;
import edu.usc.qspr.parser.qasm.QASMParser;
import edu.usc.qspr.placer.InitialPlacer;
import edu.usc.qspr.placer.InitialPlacer.Heuristic;
import edu.usc.qspr.qasm.QASM;

/**
 * The Class Main.
 */
public class Main {	
	/** The qasm. */
	private static QASM qasm;

	/** The layout. */
	private static Layout layout;

	/** The eds. */
	private static EventDrivenSimulator eds;

	/** The output file addr. */
	private static String qasmFileAddr, fabricFileAddr, outputFileAddr, placementMethod;
	
	private static PrintWriter outputFile;

	/* Seed variable */
	private static int m=1;

	/**
	 * Parses the inputs.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("static-access")
	public static void parseInputs(String [] args){
		Options options=new Options();
		
		options.addOption(OptionBuilder.withLongOpt("input")
				.withDescription( "QASM input file" )
				.hasArg()
				.withArgName("file")
				.create("i"));

		options.addOption(OptionBuilder.withLongOpt("fabric")
				.withDescription( "Fabric specification" )
				.hasArg()
				.withArgName("file")
				.create("f"));

		options.addOption(OptionBuilder.withLongOpt("output")
				.withDescription( "Quantum operation output file" )
				.hasArg()
				.withArgName("file")
				.create("o"));

		options.addOption(OptionBuilder.withLongOpt("placement")
				.withDescription( "Select a placement technique from {MVFB, MC, Center, and Baseline}." )
				.hasArg()
				.withArgName("method")
				.create("p"));

		options.addOption(OptionBuilder.withLongOpt("seed")
				.withDescription("Random seed count")
				.hasArg()
				.withArgName("#")
				.create("s"));

		
		options.addOption(OptionBuilder.withLongOpt("verbose")
				.withDescription( "Verbosely prints the quantum operations" )
				.create("v"));

		options.addOption(OptionBuilder.withLongOpt("debug")
				.withDescription( "Print debugging info" )
				.create("d"));

		CommandLineParser parser=new GnuParser();
		CommandLine cmd=null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e){
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		if (!cmd.hasOption("input")||!cmd.hasOption("fabric") || !cmd.hasOption("placement")){
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(80);
			formatter.printHelp( "qspr", "QSPR maps a given QASM " +
					"to a given PMD fabric. The resultant MCL file of " +
					"the mapped circuit will be generated.", options,"", true);

			System.exit(-1);
		}
		
		qasmFileAddr=cmd.getOptionValue("input");
		if (!new File(qasmFileAddr).exists()){
			System.err.println("QASM file "+qasmFileAddr+" does not exist.");
			System.exit(-1);
		}
		
		fabricFileAddr=cmd.getOptionValue("fabric");
		if (!new File(fabricFileAddr).exists()){
			System.err.println("Fabric file "+fabricFileAddr+" does not exist.");
			System.exit(-1);
		}
		
		placementMethod=cmd.getOptionValue("placement").toLowerCase();
		if (placementMethod.compareTo("mvfb")!=0 && 
			placementMethod.compareTo("mc")!=0 &&
			placementMethod.compareTo("center")!=0 &&
			placementMethod.compareTo("baseline")!=0){
			System.err.println("Placement technique "+cmd.getOptionValue("placement")+" is not supported. Select from {MVFB, MC, Center, and Baseline}.");
			System.exit(-1);
		}
		
		if (placementMethod.compareTo("mvfb")==0 || placementMethod.compareTo("mc")==0){
			if (!cmd.hasOption("seed")){
				System.err.println("Number of random placements (seed #) is not specified.");
				System.exit(-1);
			}else{
				try{
					m = Integer.parseInt(placementMethod=cmd.getOptionValue("seed"));
				}catch(Exception e){
					System.err.println("Seed \""+cmd.getOptionValue("seed")+"\" does not have a proper format. It should be an interger.");
					System.exit(-1);
				}
			}
		}
		
		if (cmd.hasOption("debug")){
			RuntimeConfig.DEBUG=true;
		}else{
			RuntimeConfig.DEBUG=false;
		}

		if (cmd.hasOption("verbose")){
			RuntimeConfig.VERBOSE=true;
		}else{
			RuntimeConfig.VERBOSE=false;
		}
		
		if (cmd.hasOption("output")){
			RuntimeConfig.VERBOSE=true;
			RuntimeConfig.OUTPUT_TO_FILE=true;
			outputFileAddr=cmd.getOptionValue("output");
		}else{
			RuntimeConfig.OUTPUT_TO_FILE=false;
		}
		
	}

	/**
	 * Monte Carlo
	 *
	 * @param m iteration count
	 * @param visible print the details of each iteration
	 */
	public static double mc(int m, boolean visible){
		double smallest=-1;
		double result;

		for (int i = 0; i < m; i++) {
			InitialPlacer.InitialPlacer(qasm, layout, Heuristic.ShuffledCenter);
			eds.schedule();
			result=eds.simluate();
//			if (visible){
//				System.out.println(System.getProperty("line.separator")+"------------------------------------"
//						+System.getProperty("line.separator")+"Actual Result:\t"+result+" \u00B5sec");
//				layout.usageStatistics();
//			}
			if (smallest==-1 ||smallest>result)
				smallest=result;
			layout.clean();
		}
		return smallest;
	}
	
	/**
	 * Multistart Variable-length Forward/Backward (MVFB) placer
	 *
	 * @param m iteration count
	 * @return the int
	 */
	public static double mvfb(int m){
		double result;
		double smallest=-1;
		boolean worse1, worse2, worse3;
		int totalIterations=0;


		for (int i = 0; i < m; i++) {
			worse1=worse2=worse3=false;
			if (i==0)
				InitialPlacer.InitialPlacer(qasm, layout, Heuristic.Center);
			else 
				InitialPlacer.InitialPlacer(qasm, layout, Heuristic.ShuffledCenter);
			do{
				totalIterations++;
				eds.schedule();
				result=eds.simluate();
				//	+System.getProperty("line.separator")+"Actual Result:\t"+actualResult+" \u00B5sec");
				//   	layout.usageStatistics();
				if (smallest==-1 ||smallest>result){
					smallest=result;
					worse1=worse2=worse3=false;
				}else{
					if (worse2==true){
						worse3=true;
					}else if (worse1==true){
						worse2=true;
						worse3=false;
					}else{
						worse1=true;
						worse2=false;
						worse3=false;
					}
				}
				layout.clear();
				qasm.reverseCommandsOrder();
			}while(worse3!=true);
			layout.clean();
		}
		System.out.println("MVBF total iteration count: "+totalIterations);
		return smallest;
	}

	
	/**
	 * Center placer. It places qubits at the center and performs the scheduling and routing accordingly
	 *
	 * @return the execution latency
	 */
	public static double center(){
		double result;
		
		InitialPlacer.InitialPlacer(qasm, layout, Heuristic.Center);
		eds.schedule();
		result=eds.simluate();
		return result;
	}
	
	/**
	 * Baseline: the ideal mapping without considering the routing delay
	 * 
	 * @return the execution latency
	 */ 
	public static double baseLine(){
		eds.BasicScheduling(EventDrivenSimulator.Scheduling.ASAP, qasm.getDFG());

		eds.schedule();
		double baseResult=eds.baseSimulate();
		return baseResult; 
	}

	/**
	 * Iterative placer. It starts with a central placement and iterates for k times
	 *
	 * @param k the k
	 * @return the double
	 */
	public static double Iterative(int k){
		double minResult=-1;
		double tempResult;
		
		InitialPlacer.InitialPlacer(qasm, layout, Heuristic.Center);
		for (int i = 0; i < k; i++) {
			eds.schedule();
			tempResult=eds.simluate();
			if (minResult==-1 ||minResult>tempResult)
				minResult=tempResult;

			layout.clear();
			qasm.reverseCommandsOrder();
		}
		return minResult;
	}

	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException 
	 */
	public static void main(String [] args) throws IOException{
		double result;
		long start = System.currentTimeMillis();
		//TODO: to be commented
//		args = new String("-i sample_inputs/5-1-3.qasm -f sample_inputs/fabric.ql -o output -p baseline -s 21 -v").split(" ");
//		args = new String("-i ../sample_inputs/5-1-3.qasm -f ../sample_inputs/fabric.ql -p mvfb -s 2 -v").split(" ");
		
		parseInputs(args);
		
		if (RuntimeConfig.OUTPUT_TO_FILE){
			outputFile=new PrintWriter(new BufferedWriter(new FileWriter(outputFileAddr, false)), true);
		}else{ //writing to stdout
			outputFile=new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);
		}
		
		//Prints the current working directory
		if (RuntimeConfig.DEBUG)
			System.out.println("Current directory: "+System.getProperty("user.dir"));

		layout=LayoutParser.LayoutParser(fabricFileAddr);

		qasm= QASMParser.QASMParser(qasmFileAddr);

		eds=new EventDrivenSimulator(layout, qasm.getCommandsList(), outputFile);
		

		if (placementMethod.compareTo("center")==0){
			result = center();
		}else if (placementMethod.compareTo("baseline")==0){
			result = baseLine();
		}else if (placementMethod.compareTo("mc")==0){
			result = mc(m, false);
		}else{	//default is mvfb
			result = mvfb(m);
		}
		outputFile.println("------------------------------------");
		outputFile.println("Execution latency: "+result+" us" );
		long end = System.currentTimeMillis();
		outputFile.println("QSPR runtime "+ (end - start)+" ms");
		
        if (RuntimeConfig.OUTPUT_TO_FILE){
            outputFile.close();
        }else{
            outputFile.flush();
        }

        if (RuntimeConfig.VERBOSE){
            System.out.println("Done.");
        }
	}
}