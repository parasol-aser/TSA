package edu.tamu.cse.aser.tsa;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JCaughtExceptionRef;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.callgraph.TransitiveTargets;
import soot.jimple.toolkits.infoflow.CachedEquivalentValue;
import soot.jimple.toolkits.infoflow.InfoFlowAnalysis;
import soot.jimple.toolkits.infoflow.SmartMethodInfoFlowAnalysis;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.thread.ThreadLocalObjectsAnalysis;
import soot.jimple.toolkits.thread.mhp.SynchObliviousMhpAnalysis;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.MutableDirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.util.queue.QueueReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import edu.tamu.cse.aser.tsa.thread.AbstractRuntimeThreadX;
import edu.tamu.cse.aser.tsa.thread.MultiCalledMethodsX;
import edu.tamu.cse.aser.tsa.thread.PegCallGraphX;
import edu.tamu.cse.aser.tsa.thread.RunMethodsPredX;
import edu.tamu.cse.aser.tsa.thread.Util;
import edu.tamu.cse.aser.tsa.thread.XNode;

public class ThreadSharingAnalyzer extends SceneTransformer {
  public ThreadLocalObjectsAnalysis tlo;
  //public ThreadSharedAccessAnalysis tlo;
  private static final SootMethod THREAD_START = Scene.v().loadClassAndSupport("java.lang.Thread").getMethod("void start()");

  private HashSet<String> sharedVariableLocations = new HashSet<String>();
  private HashMap<Integer,XNode> indexNodeMap = new HashMap<Integer,XNode>();

  List<AbstractRuntimeThreadX> threadList;
  


	int totalAccesses = 0;
	int sharedAccesses = 0;
	
	
	private AbstractRuntimeThreadX currentThread;
	private Byte currentThreadID = 0;
	
	//do it object sensitive  == step 2
//	private HashMap<SootField,HashSet<AbstractRuntimeThreadX>>
//		fieldThreadWriteAccesses = new HashMap<SootField,HashSet<AbstractRuntimeThreadX>>();
//	private HashMap<SootField,HashSet<AbstractRuntimeThreadX>>
//	fieldThreadReadAccesses = new HashMap<SootField,HashSet<AbstractRuntimeThreadX>>();
    public HashMap<String,Integer> variableIdMap = new HashMap<String,Integer>();
	public Integer getVariableId(String sig) {
		if(variableIdMap.get(sig)==null) {
	                  int size = variableIdMap.size() + 1;
	                  variableIdMap.put(sig, size);
	                  //variableIdSigMap.put(size, sig);

	      }
		Integer sid = variableIdMap.get(sig);

	      return sid;
	}
	
	//just to avoid stackoverflow
	HashSet<SootMethod> analyzedMethods = new HashSet<SootMethod>();
	
	  PAG pag;
	
  //InfoFlowAnalysis primitiveDfa = new InfoFlowAnalysis(true, true, printDebug);
  
  private void reportFieldRace(SootField sf)
  {
	  System.err.println(sf);
	  sharedVariableLocations.add(sf.getSignature());
  }
  private boolean isArrayAccessShared(Local local)
  {
		PointsToSetInternal pts = (PointsToSetInternal) pag.reachingObjects(local);
		return pts.forall(new P2SetVisitor() {
			
			boolean isShared ;
	          @Override
	          public void visit(Node n) {
	            if(!isShared)
	            {
		          	int id = n.getNumber();
		          	XNode xn = indexNodeMap.get(id);
		          	if(xn!=null)
		          		isShared = xn.isArrayShared();
	            }
	          }
	          @Override
	          public boolean getReturnValue()
	          {
	        	  return isShared;
	          }
	      }); 
  }
  private void accessArray(Local local, final boolean isWrite)
  {
		PointsToSetInternal pts = (PointsToSetInternal) pag.reachingObjects(local);
		pts.forall(new P2SetVisitor() {
          @Override
          public void visit(Node n) {
             
          	int id = n.getNumber();
          	XNode xn = indexNodeMap.get(id);
          	if(xn==null)
          	{
          		xn = new XNode();
          		indexNodeMap.put(id,xn);
          	}
          	xn.accessArray(currentThreadID,isWrite);
          	if(currentThread.runsMany)
          	{
              	xn.accessArray((byte) (currentThreadID+1),isWrite);
          	}

          }
      }); 
  }
  private boolean isFieldAccessShared(Local local, final SootField sf)
  {
	  if(local!=null)
	  {
		PointsToSetInternal pts = (PointsToSetInternal) pag.reachingObjects(local);
		return pts.forall(new P2SetVisitor() {
			
			boolean isShared ;
	          @Override
	          public void visit(Node n) {
	            if(!isShared)
	            {
		          	int id = n.getNumber();
		          	XNode xn = indexNodeMap.get(id);
		          	
		       	 Integer fid = getVariableId(sf.getSignature());

		       	 if(xn!=null)
		          	isShared = xn.isFieldShared(fid);
	            }
	          }
	          @Override
	          public boolean getReturnValue()
	          {
	        	  return isShared;
	          }
	      }); 
	  }
	  else
	  {
		  //static field access
        	XNode xn = indexNodeMap.get(0);
	       	 Integer fid = getVariableId(sf.getSignature());

	       	 
	          	return xn.isFieldShared(fid);
	  }
  }
  private void accessField(Local local, final SootField sf, final boolean isWrite)
  {
	  PointsToSetInternal pts = (PointsToSetInternal) pag.reachingObjects(local);
		pts.forall(new P2SetVisitor() {
          @Override
          public void visit(Node n) {
             
          	int id = n.getNumber();
          	XNode xn = indexNodeMap.get(id);
          	if(xn==null)
          	{
          		xn = new XNode();
          		indexNodeMap.put(id,xn);
          	}
          	
				accessField(id,sf,isWrite);

          }
      });                 
  }
  private void accessField(int nodeId, SootField sf, boolean isWrite)
  {
	  XNode xn = indexNodeMap.get(nodeId);
	  if(xn==null)
  		{
  		xn = new XNode();
  		indexNodeMap.put(nodeId,xn);
  		}
	  
	 Integer fid = getVariableId(sf.getSignature());
  	xn.accessField(currentThreadID,fid,isWrite);
  	
  	if(currentThread.runsMany)
  	{
      	xn.accessField((byte) (currentThreadID+1),fid,isWrite);
  	}


  }
  
  private void analyze(SootMethod sm)
  {
      if(!Util.skipPackage(sm.getDeclaringClass().getPackageName()))
	  if(sm.hasActiveBody())
	  if(!analyzedMethods.contains(sm))
	  {
	      analyzedMethods.add(sm);

	      
      //SmartMethodInfoFlowAnalysis smifa = primitiveDfa.getMethodInfoFlowAnalysis(sm);

	  Body body = sm.getActiveBody();
      Iterator<Unit> it = body.getUnits().iterator();
      while(it.hasNext()){
        Stmt stmt = (Stmt)it.next();
        if(stmt instanceof AssignStmt) // assigns a Value to a Variable
		{
			AssignStmt as = (AssignStmt) stmt;
			Value lv = as.getLeftOp();
			Value rv = as.getRightOp();
			if(!Util.isStaticConstructor(sm))
			{

				if(lv instanceof ArrayRef) // data flows into the base's data structure
				{
					ArrayRef ar = (ArrayRef) lv;
					if(ar.getBase() instanceof Local)
					{					//must be array
						accessArray((Local) ar.getBase(),true);
					
					}
	//				EquivalentValue bv = new CachedEquivalentValue(ar.getBase());
	//				List<EquivalentValue> sources =smifa.sourcesOf(bv);
	//				for(EquivalentValue source : sources)
	//                {
	//					Value v = source.getValue();
	//                    if(v instanceof FieldRef)
	//                    {
	//        				accessField(((FieldRef)v).getField(),true);
	//                    }
	//                }
				}
				else if(lv instanceof StaticFieldRef) // data flows into the field ref
				{
					
						StaticFieldRef sfr = (StaticFieldRef) lv;
						accessField(0, sfr.getField(),true);
	
				}
				else if(lv instanceof InstanceFieldRef)
				{
					final InstanceFieldRef ifr = (InstanceFieldRef) lv;

					//make sure not in constructor
					if(!Util.isInstanceConstructor(sm)
							||!Util.isSubClass(sm.getDeclaringClass(),ifr.getField().getDeclaringClass()))
					{				
						
						
						
						accessField((Local) ifr.getBase(),ifr.getField(),true);
					}
	
				}
					
				if(rv instanceof ArrayRef) // data flows from the base's data structure
				{
					ArrayRef ar = (ArrayRef) rv;
					
					if(ar.getBase() instanceof Local)
					{					//must be array
	
						accessArray((Local) ar.getBase(),false);

					}
					
				}
				else if(rv instanceof StaticFieldRef)
				{
					StaticFieldRef sfr = (StaticFieldRef) rv;
					
					accessField(0,sfr.getField(),false);
					
				}
				else if(rv instanceof InstanceFieldRef)
				{
					final InstanceFieldRef ifr = (InstanceFieldRef) rv;

				
					if(!Util.isInstanceConstructor(sm)
							||!Util.isSubClass(sm.getDeclaringClass(),ifr.getField().getDeclaringClass()))
					{	
					
						accessField((Local) ifr.getBase(),ifr.getField(),false);
					}
	 					
				}
			}
				 
			if(rv instanceof InvokeExpr)
				{
					InvokeExpr ie = (InvokeExpr) rv;
					 handleInvokeExpr(ie, as);
	
				}
			
			
		}
		else if(stmt.containsInvokeExpr()) // flows data between receiver object, parameters, globals, and return value
		{			
		    	  handleInvokeExpr(stmt.getInvokeExpr(), stmt);
		
		}
      }
      
	  }

  }
  
  private boolean checkSharing(AssignStmt as, SootMethod sm)
  {
			Value lv = as.getLeftOp();
			Value rv = as.getRightOp();
			
			if(lv instanceof ArrayRef) // data flows into the base's data structure
			{
				totalAccesses++;
				
				ArrayRef ar = (ArrayRef) lv;
				if(ar.getBase() instanceof Local)
				{					//must be array
					if(isArrayAccessShared((Local) ar.getBase()))
					{
						//System.out.println("instrument: "+as);
						return true;
					}
				}

			}
			else if(lv instanceof StaticFieldRef) // data flows into the field ref
			{
				totalAccesses++;

				StaticFieldRef sfr = (StaticFieldRef) lv;
				if(isFieldAccessShared(null,sfr.getField()))
				{
					//System.out.println("instrument: "+as);	
					return true;
				}

			}
			else if(lv instanceof InstanceFieldRef)
			{
				totalAccesses++;

				final InstanceFieldRef ifr = (InstanceFieldRef) lv;

				if(!Util.isInstanceConstructor(sm)
						||!Util.isSubClass(sm.getDeclaringClass(),ifr.getField().getDeclaringClass()))
				if(isFieldAccessShared((Local) ifr.getBase(),ifr.getField()))
					{
						//System.out.println("instrument: "+as);			
						return true;
					}

			}
						
			if(rv instanceof ArrayRef) // data flows from the base's data structure
			{
				totalAccesses++;

				ArrayRef ar = (ArrayRef) rv;
				
				if(ar.getBase() instanceof Local)
				{					//must be array

					if(isArrayAccessShared((Local) ar.getBase()))
						{
							//System.out.println("instrument: "+as);       
							return true;
						}
				
				}
			}
			else if(rv instanceof StaticFieldRef)
			{
				totalAccesses++;

				StaticFieldRef sfr = (StaticFieldRef) rv;
				
				if(isFieldAccessShared(null,sfr.getField()))
				{
					//System.out.println("instrument: "+as);					}
					return true;
				}
			}
			else if(rv instanceof InstanceFieldRef)
			{
				totalAccesses++;

					final InstanceFieldRef ifr = (InstanceFieldRef) rv;
					if(!Util.isInstanceConstructor(sm)
							||!Util.isSubClass(sm.getDeclaringClass(),ifr.getField().getDeclaringClass()))
					if(isFieldAccessShared((Local) ifr.getBase(),ifr.getField()))
					{
						//System.out.println("instrument: "+as);
						return true;
					}
			}
		
		return false;
  }
  private boolean checkSharingTLOA(AssignStmt as, SootMethod method)
  {
			Value lv = as.getLeftOp();
			Value rv = as.getRightOp();
			
			if(lv instanceof Ref) // data flows into the base's data structure
			{
				totalAccesses++;
				
				Ref ar = (Ref) lv;
				return !tlo.isObjectThreadLocal(ar, method);

			}
						
			if(rv instanceof Ref) // data flows from the base's data structure
			{
				totalAccesses++;

				Ref ar = (Ref) rv;
				
				return !tlo.isObjectThreadLocal(ar, method);

			}
		
		return false;
  }
  private void handleInvokeExpr(InvokeExpr ie, Stmt is)
  {
//	  if(is.toString().contains("hashCode"))
//		  System.out.println();
	  

		CallGraph cg = Scene.v().getCallGraph();
		for(Iterator<Edge> edges = cg.edgesOutOf(is); edges.hasNext();)
		{
			Edge e = edges.next();
			SootMethod target = e.getTgt().method();
			//if(method.getDeclaringClass().isApplicationClass())
		      if(!Util.skipPackage(target.getDeclaringClass().getPackageName()))
		      {	  
		    	  //SootMethodRef methodRef = ie.getMethodRef();
				//String subSig = methodRef.resolve().getSubSignature();//MAY NOT BE SOUND
					// Verify that this target is an implementation of the method we intend to call,
					// and not just a class initializer or other unintended control flow.
					//if(target.getSubSignature().equals(subSig))
					{
						analyze(target);
					}
		      }


		}
  }
  protected void internalTransform(String phase, Map options){

	  if(Main.doNewTLO)
		  runStaticTSA();  
	  else	  
		  runTLOA();
  }
  
/**
 * Thread local object analysis -- escape analysis
 */
private void runTLOA() {
	
		tlo = new ThreadLocalObjectsAnalysis(new SynchObliviousMhpAnalysis());
            
      //For DEBUG
//      ReachableMethods rms = Scene.v().getReachableMethods();
//      QueueReader<MethodOrMethodContext> reader = rms.listener();
//      while(reader.hasNext())
//      {
//    	  MethodOrMethodContext m = reader.next();
//    	  
//    	  System.out.println(m.method().getSignature());
//      }
      
    for(SootClass sc : Scene.v().getClasses()){
      String packageName = sc.getPackageName();
      if(Util.skipPackage(packageName)){
            //System.out.println("Skipping " + sc.getName());
            continue;
          }
      
      
      //System.out.println("class: " + sc.getName());
      for(SootMethod m : sc.getMethods()){
        if(!m.hasActiveBody() || m.hasTag("NoInstrumentTag")) {
          continue;
        }
        Body body = m.getActiveBody();
        Iterator<Unit> it = body.getUnits().snapshotIterator();
        while(it.hasNext()){
          Stmt stmt = (Stmt)it.next();
         
          if (stmt instanceof AssignStmt) 
    	    {
    	    	if(checkSharingTLOA((AssignStmt) stmt,body.getMethod()))
    	    	{
  				sharedAccesses++;

  				if(Main.printDebug)System.err.println("Shared: "+stmt);
	    			//String sig_loc = (className + "|" + methodSignature + "|" + line_cur)
	    			String sig_loc = (sc.getName() + "|" + m.getName() + "|" + Util.getLineNumber(stmt));
	    			sharedVariableLocations.add(sig_loc);
    	    	}
    	    	else
    	    		if(Main.printDebug)System.out.println("Local: "+stmt);

    	    }
    	    }
        }
       }
}
/**
 * 
 */
private void runStaticTSA() {
	{
		   threadList = new ArrayList<AbstractRuntimeThreadX>();


			SootMethod mainMethod = Util.getApplicationEntryMethod();
			ArrayList mainRunMethods = new ArrayList();
			mainRunMethods.add(mainMethod);
			// do same for main method
			AbstractRuntimeThreadX mainThread = new AbstractRuntimeThreadX(mainRunMethods);
			threadList.add(mainThread);
			
			  pag = (PAG) Scene.v().getPointsToAnalysis();
				CallGraph callGraph = Scene.v().getCallGraph();
				PegCallGraphX pecg = new PegCallGraphX(callGraph);
				Set<SootMethod> multiCalledMethods = (new MultiCalledMethodsX(pecg)).getMultiCalledMethods();
				
				
				//TODO: just need to find reachable start statements
				Set<Stmt> startStatements = new HashSet<Stmt>();
				Map<Stmt, SootMethod> startToContainingMethod = new HashMap<Stmt, SootMethod>();
				ReachableMethods rm = Scene.v().getReachableMethods();
				
				Hierarchy hierarchy = Scene.v().getActiveHierarchy();
				Iterator runAnalysisClassesIt = Scene.v().getApplicationClasses().iterator();
		    	while (runAnalysisClassesIt.hasNext()) 
		    	{
		    	    SootClass appClass = (SootClass) runAnalysisClassesIt.next();
//			    	    if(appClass.getName().contains("EclipseStarter"))
//			    	    	System.out.println();
		    	    
		    	    Iterator methodsIt = appClass.getMethods().iterator();
		    	    while (methodsIt.hasNext())
		    	    {
		    	    	SootMethod method = (SootMethod) methodsIt.next();
		    	    	if(rm.contains(method))//doesn't work for reflection-log??
		    	    	{
			    			Iterator edgesIt = callGraph.edgesOutOf( method );
			    			while(edgesIt.hasNext())
			    			{
			    				SootMethod target = ((Edge) edgesIt.next()).tgt();
			    				if(target== THREAD_START)
			    				{
					    	    	Body b = method.retrieveActiveBody();
					    	    	
									Iterator stmtIt = b.getUnits().iterator();
									while(stmtIt.hasNext())
									{
										Stmt stmt = (Stmt) stmtIt.next();
									    if (stmt.containsInvokeExpr()) 
										if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr)
									        if (stmt.getInvokeExpr().getMethod() == THREAD_START)
														// This is a Thread.start()
														if(!startStatements.contains(stmt))
														{
															startStatements.add(stmt);
															startToContainingMethod.put(stmt, method);
														}
												
											
										
									}
								}
				    	    }
				    	}
		    	
		    	    }
		    	}
				if(!startStatements.isEmpty())
				{
					HashMap<Stmt, List<SootMethod>> startToRunMethods = new HashMap<Stmt, List<SootMethod>>();

					TransitiveTargets runMethodTargets = new TransitiveTargets( callGraph, new Filter(new RunMethodsPredX()) );
					
					Iterator<Stmt> startIt = startStatements.iterator();
					while (startIt.hasNext())
					{
						Stmt start = startIt.next();
						
						List<SootMethod> runMethodsList = new ArrayList<SootMethod>(); // will be a list of possible run methods called by this start stmt
						
						// Get possible thread objects (may alias)
						Value startObject = ((InstanceInvokeExpr) (start).getInvokeExpr()).getBase();
							
						// For each possible thread object, get run method
						Iterator<MethodOrMethodContext> mayRunIt = runMethodTargets.iterator( start ); // fails for some call graphs
						while( mayRunIt.hasNext() )
						{
							SootMethod runMethod = (SootMethod) mayRunIt.next();
							if( runMethod.getSubSignature().equals("void run()") )
							{
								runMethodsList.add(runMethod);
							}
						}
						
						// If haven't found any run methods, then use the type of the startObject,
						// and add run from it and all subclasses
						if(runMethodsList.isEmpty() && ((RefType) startObject.getType()).getSootClass().isApplicationClass())
						{
							List<SootClass> threadClasses = hierarchy.getSubclassesOfIncluding( ((RefType) startObject.getType()).getSootClass() );
							Iterator<SootClass> threadClassesIt = threadClasses.iterator();
							while(threadClassesIt.hasNext())
							{
								SootClass currentClass = threadClassesIt.next();
								if( currentClass.declaresMethod("void run()") )							
								{
									runMethodsList.add(currentClass.getMethod("void run()"));
								}
							}
						}
						
						startToRunMethods.put(start, runMethodsList);
						
						System.err.println("New Thread: "+start+" -> "+runMethodsList);
					}
				
		    	
			    	for(Stmt startStmt: startStatements)
			    	{
			    		SootMethod startStmtMethod = startToContainingMethod.get(startStmt);
						AbstractRuntimeThreadX thread = new AbstractRuntimeThreadX( startToRunMethods.get(startStmt)); // provides a list interface to the methods in a thread's sub-call-graph
						if(multiCalledMethods.contains(startStmt)||multiCalledMethods.contains(startStmtMethod))
						{
							thread.runsMany = true;
						}
						
						 //Add this list of methods to MHPLists
						threadList.add(thread);
			    	}
				}
			
			if(threadList.size()>0)
			{
				for(AbstractRuntimeThreadX t:threadList)
				{
					currentThread =t;
					currentThreadID++;
					
					
					analyzedMethods.clear();
					
					for(SootMethod run : t.getRunMethods())//how can it have multiple run methods? inheritency?
					{
						
						analyze(run);
					}
					
					if(t.runsMany)					
						currentThreadID++;

				}
			

			
		    for(SootClass sc : Scene.v().getClasses()){
		        String packageName = sc.getPackageName();
		        if(Util.skipPackage(packageName)){
		              //System.out.println("Skipping " + sc.getName());
		              continue;
		            }
		        
		        
		        //System.out.println("class: " + sc.getName());
		        for(SootMethod m : sc.getMethods()){
		          if(!m.hasActiveBody() || m.hasTag("NoInstrumentTag")) {
		            continue;
		          }
		          Body body = m.getActiveBody();
		          Iterator<Unit> it = body.getUnits().snapshotIterator();
		          while(it.hasNext()){
		            Stmt stmt = (Stmt)it.next();
		           
			            if (stmt instanceof AssignStmt) { 
			      	    
			      	    	if(checkSharing((AssignStmt) stmt, m))
			      	    	{
			    				sharedAccesses++;

			    				if(Main.printDebug)System.err.println("Shared: "+stmt);
			      	    		
			      	    		{
			      	    			//String sig_loc = (className + "|" + methodSignature + "|" + line_cur)
			      	    			String sig_loc = (sc.getName() + "|" + m.getName() + "|" + Util.getLineNumber(stmt));
			      	    			sharedVariableLocations.add(sig_loc);
			      	    		}
			      	    	}
			      	    	else
			      	    		if(Main.printDebug)System.out.println("Local: "+stmt);

			      	    
			          }
		          }
		        }
		      }
			}

			
		return;	
	  }
}
  private void println(String message)
  {
	  //to log file
	  G.v().out.println(message);
	  //to terminal
	  System.out.println(message);
  }
  public void reportSharedVariables()
  {
	  
	// Serialize / save it
	  try {
		  ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tmp/sharedLocations"));
		oos.writeObject(sharedVariableLocations);
		oos.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	 println(
              "****************************************************");
	  println("* "+sharedVariableLocations.size()+
              " Shared Access Locations *");
          println(
              "****************************************************");
		for (String s: sharedVariableLocations) 
		{
			println(s);
		}
		
		println(
              "****************************************************");
		println("* shared/total "+ sharedAccesses+ "/"+totalAccesses
              +" *");
		println(
              "****************************************************");
		println(
	              "****************************************************");
		int size = 0;
		if(threadList!=null)size=threadList.size();
		if(tlo!=null)
			size = tlo.mhp.getThreads().size();
			println("Total abstract threads: "+size
	              +" *");
			println(
	              "****************************************************");
  }
  
  public Set<String> getSharedVariableSignatures()
  {
	  return sharedVariableLocations;
  }

public boolean isShared(String sig) {
	return sharedVariableLocations.contains(sig);
}
}
