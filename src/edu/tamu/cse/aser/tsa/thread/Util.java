package edu.tamu.cse.aser.tsa.thread;

import java.util.LinkedList;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;

public class Util {

	  public static final String instanceConstructorName = "<init>";
	  public static final String staticInitializerName = "<clinit>";
	  public static final String threadClassName = "java.lang.Thread";
	  
	  public final  static LinkedList<String> excludeList = new LinkedList<String> ();
	  public final  static LinkedList<String> includeList = new LinkedList<String> ();

	    static
	    {
	    // the following packages are excluded in Soot by default
	    //	java., sun., javax., com.sun., com.ibm., org.xml., org.w3c., apple.awt., com.apple.
	    excludeList.add("aser.");
	    excludeList.add("edu.");
	    excludeList.add("java.");
	    excludeList.add("javax.");
	    excludeList.add("sun.");
	    excludeList.add("sunw.");
	    excludeList.add("com.sun.");
	    excludeList.add("com.ibm.");
	    excludeList.add("com.apple.");
	    excludeList.add("apple.awt.");
	    excludeList.add("org.xml.");
	    excludeList.add("jdbm.");
	    
	    includeList.add("java.util.");
	    includeList.add("java.lang.");

	    includeList.add("org.w3c.");//for jigsaw
	    includeList.add("org.apache.");//for apache
	    //includeList.add("java.");
	    //includeList.add("javax.");
	    //includeList.add("sun.");
	    //includeList.add("sunw.");

	    }
	    public static boolean skipPackage(String packageName)
	    {
	    	for(String name : excludeList)
	    	if(packageName.startsWith(name))
	    		return true;

	    		return false;
	    }
	  public static String mkStars(float t){
	    String s = String.valueOf(t);
	    String stars = "";
	    for(int i = 0; i < s.length(); ++i) stars += "*";
	    return stars;
	  }  
	  public final static boolean isThreadSubClass(SootClass c)
	  {
//		  List<SootClass> superClasses = hierarchy.getSuperclassesOfIncluding(
		  return false;
	  }
	  
	  public static boolean isSubClass(SootClass a, SootClass b)
	  {		  
		  while(!a.getName().equals("java.lang.Object"))
		  {
			  if(a==b)
				  return true;
			  a = a.getSuperclass();
			  
		  }
		  return false;
	  }
	public final static boolean isThreadClass(SootClass c)
	{
		if(c.getName().equals(threadClassName))
			return true;
		
		return false;
	}
	
	/**
	 * @param sm
	 * @return
	 */
	public static boolean shouldAnalyzeMethod(SootMethod sm) {
		return sm.isConcrete() && 
				(sm.getDeclaringClass().isApplicationClass()&&!skipPackage(sm.getDeclaringClass().getPackageName())||sm.isPublic()&&isThreadClass(sm.getDeclaringClass()));
	}
	
	private static SootMethod entryMethod;
	public static SootMethod getApplicationEntryMethod()
	{
		if(entryMethod!=null)
			return entryMethod;
		
		return Scene.v().getMainClass().getMethod("void main(java.lang.String[])");

	}
	
	public static boolean isStaticConstructor(SootMethod sm)
	{
		if(sm.getName().equals(staticInitializerName))
			return true;
		else
			return false;
	}
	public static boolean isInstanceConstructor(SootMethod sm)
	{
		if(sm.getName().equals(instanceConstructorName))
			return true;
		else
			return false;
	}
	
	  public static int getLineNumber(Stmt stmt)
	  {
		  Tag tag =  stmt.getTag("LineNumberTag");
		  if(tag==null)
			  return 0;
		  else
			  return ((LineNumberTag)tag).getLineNumber();
	  }
	public static void setEntryMethod(SootMethod m) {
		entryMethod = m;
	}
}
