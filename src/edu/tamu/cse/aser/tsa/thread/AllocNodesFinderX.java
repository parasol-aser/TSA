package edu.tamu.cse.aser.tsa.thread;

import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.jimple.toolkits.callgraph.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.spark.pag.*;
import java.util.*;

// *** USE AT YOUR OWN RISK ***
// May Happen in Parallel (MHP) analysis by Lin Li.
// This code should be treated as beta-quality code.
// It was written in 2003, but not incorporated into Soot until 2006.
// As such, it may contain incorrect assumptions about the usage
// of certain Soot classes.
// Some portions of this MHP analysis have been quality-checked, and are
// now used by the Transactions toolkit.
//
// -Richard L. Halpert, 2006-11-30


public class AllocNodesFinderX{
	
	private final Set<AllocNode> allocNodes;
	private final Set<AllocNode>  multiRunAllocNodes;
	MultiCalledMethodsX mcm;
	PAG pag;
	
	public  AllocNodesFinderX(PegCallGraphX pcg, CallGraph cg, PAG pag){
		//System.out.println("===inside AllocNodesFinder===");
		this.pag = pag;
		allocNodes = new HashSet<AllocNode>();
		multiRunAllocNodes = new HashSet<AllocNode>();
		
		mcm = new MultiCalledMethodsX(pcg);
		
		find(mcm.getMultiCalledMethods(), pcg, cg);
	}
	private void find(Set<SootMethod> multiCalledMethods, PegCallGraphX pcg, CallGraph callGraph){
		Set clinitMethods = pcg.getClinitMethods();
		Iterator it = pcg.iterator();
		while (it.hasNext()){
		SootMethod sm = (SootMethod)it.next();
			UnitGraph graph = new CompleteUnitGraph(sm.getActiveBody());
			Iterator iterator = graph.iterator();
			if (multiCalledMethods.contains(sm)){
				while (iterator.hasNext()){
					Unit unit = (Unit)iterator.next();
					//System.out.println("unit: "+unit);
					if (clinitMethods.contains(sm)  && unit instanceof AssignStmt){
						//Value rightOp = ((AssignStmt)unit).getRightOp();
						
						//Type type = ((NewExpr)rightOp).getType();//Commented by Jeff
						
						AllocNode allocNode = pag.makeAllocNode(
								PointsToAnalysis.STRING_NODE,
								RefType.v( "java.lang.String" ), null );
						//AllocNode allocNode = pag.makeAllocNode((NewExpr)rightOp, type, sm);
						//  System.out.println("make alloc node: "+allocNode);
						allocNodes.add(allocNode);
						multiRunAllocNodes.add(allocNode);
						
					}
					
					else if (unit instanceof DefinitionStmt ){
						Value rightOp = ((DefinitionStmt)unit).getRightOp();
						if (rightOp instanceof NewExpr){
							Type type = ((NewExpr)rightOp).getType();
							AllocNode allocNode = pag.makeAllocNode(rightOp, type, sm);
							//System.out.println("make alloc node: "+allocNode);
							allocNodes.add(allocNode);
							multiRunAllocNodes.add(allocNode);
						}
					}
				} 
			}
			
			else{
				// MultiRunStatementsFinder finder = new MultiRunStatementsFinder(graph, sm);     
				MultiRunStatementsFinderX finder = new MultiRunStatementsFinderX(graph, sm, multiCalledMethods, callGraph);
				FlowSet fs = finder.getMultiRunStatements();
				//methodsToMultiObjsSites.put(sm, fs);
				//     PatchingChain  pc = sm.getActiveBody().getUnits();
				
				while (iterator.hasNext()){
					Unit unit = (Unit)iterator.next();
					//System.out.println("unit: "+unit);
					
					if (clinitMethods.contains(sm)  && unit instanceof AssignStmt){
						AllocNode allocNode = pag.makeAllocNode(
								PointsToAnalysis.STRING_NODE,
								RefType.v( "java.lang.String" ), null );
						//   AllocNode allocNode = pag.makeAllocNode((NewExpr)rightOp, type, sm);
						//System.out.println("make alloc node: "+allocNode);
						allocNodes.add(allocNode);
						/*if (fs.contains(unit)){
						 multiRunAllocNodes.add(unit);
						 }*/
					}
					else if (unit instanceof DefinitionStmt ){
						
						Value rightOp = ((DefinitionStmt)unit).getRightOp();
						if (rightOp instanceof NewExpr){
							Type type = ((NewExpr)rightOp).getType();
							AllocNode allocNode = pag.makeAllocNode(rightOp, type, sm);
							//System.out.println("make alloc node: "+allocNode);
							allocNodes.add(allocNode);
							if (fs.contains(unit)){
								//System.out.println("fs contains: "+unit);
								multiRunAllocNodes.add(allocNode);
							}
						}
					}
				}
			}
		}
	}
	
	public Set<AllocNode> getAllocNodes()
	{
		return allocNodes;
	}
	
	public Set<AllocNode> getMultiRunAllocNodes()
	{
		return  multiRunAllocNodes;
	}
	
	public Set<SootMethod> getMultiCalledMethods()
	{
		return mcm.getMultiCalledMethods();
	}
}
