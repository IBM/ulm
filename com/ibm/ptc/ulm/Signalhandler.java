// Copyright © 2005, 2018 IBM Corp. * Licensed under the Apache license

package com.ibm.ptc.ulm;

import sun.misc.Signal;
import sun.misc.SignalHandler;
/**
 * Signalhandler ensures that when the program is exited the output string is closed.
 * This is of particular importance when compressing.
 *
 */
public class Signalhandler implements SignalHandler {
	       
	    private SignalHandler oldHandler;
	    static Master master;

	    // Static method to install the signal handler
	    public static Signalhandler install(String signalName, Master ma) {
	    	master = ma;
	        Signal diagSignal = new Signal(signalName);
	        Signalhandler diagHandler = new Signalhandler();
	        diagHandler.oldHandler = Signal.handle(diagSignal,diagHandler);
	        return diagHandler;
	    }

	    // Signal handler method
	    public void handle(Signal sig) {
	        System.out.println("Diagnostic Signal handler called for signal "+sig);
	        try {System.out.println("Stopped monitoring, closing output string.");
	        	master.closeOutputString();
	        	/*
	        }
	            // Output information for each thread
	            Thread[] threadArray = new Thread[Thread.activeCount()];
	            int numThreads = Thread.enumerate(threadArray);
	            System.out.println("Current threads:");
	            for (int i=0; i < numThreads; i++) {
	                System.out.println("    "+threadArray[i]);
	            }
	            
	            // Chain back to previous handler, if one exists
	            if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
	                oldHandler.handle(sig);
	            }
	            */
	        	System.exit(0);
	        	if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
	                oldHandler.handle(sig);
	        	}

	        } catch (Exception e) {
	            System.out.println("Signal handler failed, reason "+e);
	            e.printStackTrace();
	        }
	    }
	}
