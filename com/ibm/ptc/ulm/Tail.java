// Copyright © 2005, 2018 IBM Corp. * Licensed under the Apache license

package com.ibm.ptc.ulm;
import java.io.*;
import java.util.HashSet;
/**
 * Tail looks for lines being added to a log file and outputs them to a Master object.
 * An instance of Tail is created by ULM for each log file to be monitored.
 * 
 * @see Master
 * @see ULM
 * @author aptjarvis
 *
 */
public class Tail implements Runnable {
	String filename, quickname;
	int delay;
	Master mas;
	boolean skipStart = true;
	/**
	 * Tail method checks for lines being added to a log file. When a line is discovered it 
	 * is output to the master object
	 * 
	 * @param filename the path of the logfile to monitor
	 * @param quickname a name attached to the beginning of each line as a reminder of which 
	 * log it came from 
	 * @param delay the sleep time between each check of the log file, in miliseconds
	 * @param ma the Master object to send each new line to
	 */
	public Tail (String filename, String quickname, int delay, Master ma){
		this.filename = filename;
		this.quickname = quickname;
		this.delay = delay;
		mas = ma;
		//		this.option = option;
	}
	// This is only used internally to set fromStart when we're monitoring a directory
	private Tail (String filename, String quickname, int delay, Master ma, boolean fromStart){
		this.filename = filename;
		this.quickname = quickname;
		this.delay = delay;
		this.skipStart = !fromStart;
		mas = ma;
	}
	public void run(){
		File f = new File(filename);
		BufferedReader in;
                if ( !f.isDirectory() ) {
		    try {
			while (! f.exists()) {
				Thread.sleep(50);
			}
			in = new BufferedReader(new FileReader(f));
			String line;
			if ( skipStart )
			{
			    boolean skipLine = true;
			    while(skipLine)
			    {
				if ((line = in.readLine()) == null){
					skipLine = false;
				}
			    }
			}
			// call mas.newDest() when you want to change log file.
			while (true){
					if ((line = in.readLine()) != null){
						mas.outputString(quickname + ":" + line);
					} else {
						Thread.sleep(delay);
					}
				}
		    }
		    catch(IOException e){
			System.out.println("Exception detected in Tail: " + e);
		    }
		    catch (InterruptedException e2) {
			System.out.println("Exception detected in Tail: " + e2);
		    }
		} else { // f.isDirectory
		    // Make a hash of existing files, so we remember what to ignore
		    String[] newfilelist = f.list();
		    HashSet filelist = new HashSet();
		    for ( int i=0; i<newfilelist.length; i++ )
		       if ( !new File(newfilelist[i]).isDirectory() )
		          filelist.add(newfilelist[i]);

		    int suffix=0;
	            while (true)
	            {
	            	try {
			    Thread.sleep(5000);
			} catch (java.lang.InterruptedException ie) {} 
			newfilelist = f.list();
			for ( int i=0; i<newfilelist.length; i++ )
			    if ( !filelist.contains(newfilelist[i]) && !new File(newfilelist[i]).isDirectory() )
			    {
			        mas.outputString(quickname+suffix + ":*** ULM *** Starting processing of new file " + newfilelist[i] + " in " + filename);
				Thread t = new Thread( new Tail(filename + "/" + newfilelist[i], quickname+suffix, delay, mas, true) );
				suffix++;
				t.start();
				filelist.add(newfilelist[i]);
			    }
		    }
		}
	}
}

