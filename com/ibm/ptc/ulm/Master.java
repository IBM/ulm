// Copyright © 2005, 2018 IBM Corp. * Licensed under the Apache license

package com.ibm.ptc.ulm;
import java.io.*;
import java.util.zip.*;

/**
 * Master is a class to provide a single point of output for multiple inputs. It has a method 
 * that takes an input String (which will come from a log file via Tail or Tail matcher) and 
 * outputs this to a file in the appropriate format as set up by the logDest method. The output 
 * can be compressed, uncompressed or sent to System out. It also has a method to change the 
 * file being written to. 
 * 
 * @see Tail
 * @see TailMatcher
 * @author aptjarvis
 *
 */
public class Master {
	PrintWriter out;
	FileOutputStream fos;
	GZIPOutputStream gzos;
	boolean compress;
	String filename;
	int logNumber = 1;
	boolean nofile = false;
	/**
	 * The logDest method sets the output for the logs to be written to. This can be to a file
	 * compressed, non compressed or, if filename is null, output to System out.
	 * If the filename already exists it returns false.
	 * 
	 * @param filename name of the destination file.
	 * @param compress weather the file should be compressed or not
	 * @return This returns true unless the file already exists.
	 */
	public boolean logDest(String filename, boolean compress){
		this.filename = filename;
		this.compress = compress;
		boolean fileClear = false;
		if (filename == null || filename.equals("")){
			nofile = true;
			fileClear = true;
		} else if (compress){
			String f = filename + "." + logNumber + ".gz";
			try {
				File file = new File(f);
				if (file.exists()){
					fileClear = false;
				} else {
					fos = new FileOutputStream(f);
					gzos = new GZIPOutputStream(fos);
					out = new PrintWriter(gzos, true);
					fileClear = true;
				}
			} catch (FileNotFoundException e) {
				System.out.println("Exception: "+e);
			} catch (IOException e2) {
				System.out.println("Exception: "+e2);
			}
		} else {
			try{
				String f = filename + "." + logNumber;
				File file = new File(f);
				if (file.exists()){
					fileClear = false;
				} else {
					out = new PrintWriter(new FileWriter(f, true), true);
					fileClear = true;
				}
			} catch (IOException e) {
				System.out.println("Exception: " + e);
			}
		}
		return fileClear;
	}
	
	/**
	 * The method newDest is called to change the output file. This is done by increasing the
	 * suffix of the filename by one.
	 */
	public void newDest(){
		if (nofile){
			
		} else {
			out.close();
			logNumber++;
			if (compress){
				String f = filename + "." + logNumber + ".gz";
				try {
					fos = new FileOutputStream(f);
					gzos = new GZIPOutputStream(fos);

					out = new PrintWriter(gzos, true);
				} catch (FileNotFoundException e) {
					System.out.println("Exception: "+e);
				} catch (IOException e2) {
					System.out.println("Exception: "+e2);
				}
			} else {
				try{
					String f = filename + "." + logNumber;
					out = new PrintWriter(new FileWriter(f, true), true);
				} catch (IOException e) {
					System.out.println("Exception: " + e);
				}
			}
		}
	}
	/**
	 * The method outputString takes a String and outputs to the printWriter out or System out.
	 * 
	 * @param in The string to be output
	 */
	synchronized public void outputString(String in) {
		if (nofile){
			System.out.println(in);
		}else{
			out.println(in);
			// System.out.println(in);
			out.flush();
		}
	}
	/**
	 * The closeOutputString method is called to close the output. This must be called after 
	 * all the useful input has been output. It will ensure the output is formed properly.
	 */
	public void closeOutputString(){
		if (!nofile)
			out.close();
	}
	
/**
 *  Unused.
 */
	public void openOutputString(){
		if (compress){
			try {
				fos = new FileOutputStream(filename, true);
				gzos = new GZIPOutputStream(fos);
				out = new PrintWriter(gzos, true);
			} catch (FileNotFoundException e) {
				System.out.println("Exception: "+e);
			} catch (IOException e2) {
				System.out.println("Exception: "+e2);
			}
		} else {
			try{
				out = new PrintWriter(new FileWriter(filename, true), true);
			} catch (IOException e) {
				System.out.println("Exception: " + e);
			}
		}
	}
}

