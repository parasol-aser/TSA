
package edu.tamu.cse.aser.tsa.thread;

import java.util.List;

import soot.SootMethod;

public class AbstractRuntimeThreadX
{

	public List<SootMethod> runMethods; // meant to be a subset of methods

	// What kind of parallelism
	public boolean runsMany;



	public AbstractRuntimeThreadX(List<SootMethod> methods)
	{
		this.runMethods = methods; 
		// What kind of parallelism - this is set unsafely, so analysis MUST set it correctly
	}
	public List<SootMethod> getRunMethods() {
		return runMethods;
	}
}
