// Copyright © 2005, 2018 IBM Corp. * Licensed under the Apache license

package com.ibm.ptc.ulm;
import java.util.Properties;
import java.lang.Thread;
import java.io.*;

/** ULM contains the Main method for ulm (Universal Logfile Mulitplexer), a tool for
 * monitoring any number of logs. For each log an instance of Tail or TailMatcher is called 
 * to monitor that log, which ensures that when these input logs are written to, each new line 
 * is copied in order to the specified output log file(or to the screen if not). The Master
 * object manages the multiplexing of the output.
 * 
 * @see Tail
 * @see TailMatcher
 * @author aptjarvis
 *
 */

public class ULM {
	/** A properties file or <br> [optional log file destination plus] list of files 
	 * with quicknames. <br>
	 * <b> example arguments </b> <br> /log.properties <br>
	 * /logdest.log /logfile1 L1 /logfile2 L2 etc
	 * 
	 * @param args A properties file or (optional log file destination plus) list of files 
	 * with quicknames.
	 * If no log file destination specified then output is sent to System out.
	 * This can take as argument a properties file, in which case more options can be 
	 * specified by adding lines to it options include: output compression (eg add compress = 
	 * true), logfile monitoring delay in miliseconds (eg delay = 50), one or two regular 
	 * expressions (eg pattern1 = ^testStart.*now) which, when discovered, trigger the output 
	 * to go to a new logfile <br>
	 * Alternatively it will take a list of logfiles each followed by a quickname
	 * To specify a logfile destination put this as a first perameter before the list of 
	 * logfiles. <br>
	 * 
	 */
	public static void main(String[] args) {
		int i = 1;
		/**
		 * The class master is used as the output and controls the compressing
		 * 
		 * @see com.ibm.ptc.ulm.Master
		 */
		Master ma = new Master();
		try {
		Signalhandler.install("INT", ma);
		}catch (NullPointerException e){
			System.out.println("Exception: " +e);
			}
		if (args.length==0){
			System.out.println("Usage: java -jar ULM.jar (file.properties|[logfile](filename quickname)*)");
			System.out.println("See http://github.com/ibm/ulm properties documentation");
			System.out.println("'quickname' will be prefixed on each line from the corresponding file");
			System.out.println("filename may also be a directory to monitor for new files");
			System.out.println("If 'logfile' is not specified, output will be sent to the console");
		} else {
		boolean comp = false;
		//check for properties file as first argument
		if (args[0].endsWith("properties")){
			File propfile = new File(args[0]);
			Properties files = new Properties();
			//load properties file
			try {
				files.load(new FileInputStream(propfile));
			} catch (FileNotFoundException e) {
				System.out.println("Exception in ULM: "+ e);
			} catch (IOException e2) {
				System.out.println("Exception in ULM: "+ e2);
			} catch (NumberFormatException e3) {
				System.out.println("Exception in ULM:"+ e3);
			}
			System.out.println("Properties file found: "+ args[0]);
			try {
				String dest;
				if (args.length != 1) {
					dest = args[1];
				} else {
					dest = files.getProperty("logDest", null);
				}
				//check  for compress option
				if (files.getProperty("compress").equals("true")){
					comp = true;
					System.out.println("compressing log file");
//					dest = dest + ".gz";
				}
				if (!ma.logDest(dest, comp)){
					System.out.println("ERROR");
					System.out.println(dest+" not a suitable destination.");
					System.out.println("Enter a new filename or remove all the logs with this prefix.");
				} else {
					System.out.println("output being written to "+ dest);
					//check for delay, default = 100
					int d = Integer.parseInt(files.getProperty("delay", "100"));
					System.out.println("Refresh delay set to = " + files.getProperty("delay", 
							"100")+ "milliseconds");
					System.out.println("Monitoring...");
					//start threads
					while (files.getProperty("file"+i) != null){
						if (files.getProperty("pattern1") != null){
							String A = files.getProperty("pattern1");
							String B = files.getProperty("pattern2");
							Thread t = new Thread(new TailMatcher(files.getProperty("file"+i), 
									files.getProperty("name"+i, "--"), d, ma, A, B));
							System.out.println(files.getProperty("file"+i));
							//System.out.println(A+B);
							t.start();
							i++;
						} else {
							Thread t = new Thread(new Tail(files.getProperty("file"+i), 
									files.getProperty("name"+i, "--"), d, ma));
							System.out.println(files.getProperty("file"+i));
							i++;
							t.start();
						}
					}
				}
			} catch (NumberFormatException e3) {
				System.out.println("Exception: "+ e3);
			}
		} else {
			//if no properties file then assume arguments contain log files to monitor
			i = args.length;
			System.out.println(i + " arguments");
			//check for a log destination
			if (i%2 == 0){
				ma.logDest(null, false);
				System.out.println("output being written to screen");
				System.out.println("Monitoring...");
				//start threads
				while (i > 0){					
					Thread t = new Thread(new Tail(args[i-2], args[i-1], 100, ma));
					System.out.println(args[i-2]);
					i = i-2;
					t.start();
				}
			} else {
				//log destination specified
				ma.logDest(args[0], false);
				System.out.println("output will be written to " + args[0]);
				System.out.println("Is this correct? yes/no");
				//check correct log destination specified
				while (true){
					BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
					String ans;
					try {
						ans = console.readLine();
						if (ans.equals("yes")){ 
							System.out.println("Monitoring...");
							//start threads
							while (i > 1){
								Thread t = new Thread(new Tail(args[i-2], args[i-1], 100, ma));
								System.out.println(args[i-2]);
								i = i-2;
								t.start();
							}
							break;
						} else if (ans.equals("no")){
							System.out.println("Please enter log destination as first argument");
							break;
						} else {
							System.out.println("Answer yes or no");
						}
				
					}
					catch (IOException e) {
						System.out.println("Exception "+e);
					}
				}
			}
			}
		}
		//bit of code to manually close out stream useful to close gzip.

/*			while (comp){
				BufferedReader ex = new BufferedReader(new InputStreamReader(System.in));
				String reply;
				try{
					System.out.println("type close to close output string");
					reply = ex.readLine();
					if (reply.equals("close")){
						ma.closeOutputString();
						System.out.println("closed");
						break;
					}
				
				} catch (IOException e) {
					System.out.println("Exception "+e);
				} catch (NullPointerException e) {
					System.out.println("bla");
					e.printStackTrace();
					//System.exit(0);
				}
			} */
	
	}
}
