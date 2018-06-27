// Copyright © 2005, 2018 IBM Corp. * Licensed under the Apache license

package com.ibm.ptc.ulm;
import java.io.*;
import java.util.regex.*;
/**
 * TailMatcher looks for lines being added to a log file and outputs them to a Master object.
 * An instance of TailMatcher is created by ULM for each log file to be monitored.
 * When a line is added to the log it is checked for the presence of the provided pattern(s).
 * When a pattern is found the Master object is told to write to a new log file
 * 
 * @see Master
 * @see ULM
 * @author aptjarvis
 *
 */
public class TailMatcher implements Runnable {
	Pattern pat1;
	Pattern pat2;
	Matcher mat1, mat2;
	String filename, quickname, pattern1, pattern2;
	int delay, i=0;
	Master mas;
	/**
	 * TailMatcher method checks for lines being added to a log file. When a line is discovered 
	 * it is output to the master object
	 * 
	 * @param filename the path of the logfile to monitor
	 * @param quickname a name attached to the beginning of each line as a reminder of which 
	 * log it came from 
	 * @param delay the sleep time between each check of the log file, in miliseconds
	 * @param ma the Master object to send each new line to
	 * @param pattern1 a pattern to search for
	 * @param pattern2 a pattern to search for
	 */
	public TailMatcher (String filename, String quickname, int delay, Master ma, String pattern1, String pattern2){
		this.filename = filename;
		this.quickname = quickname;
		this.delay = delay;
		pat1 = Pattern.compile(pattern1);
		pat2 = Pattern.compile(pattern2);
		mas = ma;
		//		this.option = option;
	}
	public void run(){
		File f = new File(filename);
		BufferedReader in;
		try {
			while (! f.exists()) {
				Thread.sleep(50);
			}
			in = new BufferedReader(new FileReader(f));
			String line;
			boolean skipLine = true;
//			if (false /*option.equals("all")*/ ){
//				while(i < 1200)
//				{
//					i++;
//					if ((line = in.readLine()) != null){
//					mas.outputString(quickname + ";" + line);
//					} else {
//						Thread.sleep(delay);
//					}
//			}
//				in.close();	
//			} else {
			while(skipLine)
			{
				if ((line = in.readLine()) == null){
					skipLine = false;
				}
			}
			// call mas.newDest() when you want to change log file.
			while (true){
					if ((line = in.readLine()) != null){
						mat1 = pat1.matcher(line);
						mat2 = pat2.matcher(line);
						//if statement for changing to new file before writing line
						if (mat1.matches() || mat2.matches()){
							
							mas.outputString(quickname + ":" + line);
							mas.outputString("Found match, changing log file");
							mas.newDest();
							System.out.println("Found match");
						}
						mas.outputString(quickname + ":" + line);
						// put if statement here for changing to new file after
						// writing line.
					} else {
						Thread.sleep(delay);
					}
				}
//			}
//			in.close();	
		}
		catch(IOException e){
			System.out.println("Exception detected in TailMatcher: " + e);
		}
		catch (InterruptedException e2) {
			System.out.println("Exception detected in TailMatcher: " + e2);
		} 
	}
}

