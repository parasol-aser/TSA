package edu.tamu.cse.aser.tsa;

import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;
import soot.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.PrintStream;

import edu.tamu.cse.aser.tsa.profile.Config;
import edu.tamu.cse.aser.tsa.thread.Util;
import soot.jimple.IdentityStmt;
import soot.jimple.spark.SparkTransformer;


public class Main{
	private static String DIR_TMP = "tmp";
	static boolean outputJimple = false;
	
	static boolean includeLibrary = false;
	static  boolean doNewTLO = true;
	
	static	boolean printDebug = true;
	
  public static void main(String[] args) {
    /* check the arguments */
    if (args.length < 1) {
      System.err.println("Usage: java Main [options] classname");//classpath
      System.exit(1);
    }
    
      System.out.println(
          "****************************************************");
      System.out.println(
          "* Loading and configuring SOOT for instrumentation *");
      System.out.println(
          "****************************************************");
      
      String mainclass = args[0];
            

      //print tag
      //Options.v().set_verbose(true);

	  long time = System.currentTimeMillis();
	  
	  setOptions(DIR_TMP);
      
      setClassPath();
      enableSpark();
      
      ThreadSharingAnalyzer sharingAnalyzer = new ThreadSharingAnalyzer();
      PackManager.v().getPack("wjtp").add(new Transform("wjtp.ThreadSharing", sharingAnalyzer));

      SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
      
      try{
      Scene.v().setMainClass(appclass);
      }catch(Exception e)
      {
    	  //if a main method is not available in the mainClass
    	  //then use the one specified in tsa.entry
    	  if(Config.instance.entryMethod!=null)
    	  {
    		  SootMethod entryMethod = appclass.getMethodByName(Config.instance.entryMethod);
    		  List entryPoints = new ArrayList();
    		  entryPoints.add(entryMethod);
    		  Scene.v().setEntryPoints(entryPoints);
    		  
    		  Util.setEntryMethod(entryMethod);
    	  }
      }
      
      Scene.v().loadNecessaryClasses();
      float t = ((System.currentTimeMillis() - time)/ 1000f);
      String stars = Util.mkStars(t);
      System.out.println("*****************************" + stars);
      System.out.println("* Finished loading Soot " 
          + "[" + t + "s] * ");
      System.out.println("*****************************" + stars);
      System.out.println(
      "**************************************************************");
      System.out.println(
      "* Instrumenting all necessary classes...                     *");
      System.out.println(
      "* this may take a while as necessary libraries are pulled in *");
      System.out.println(
      "**************************************************************");
      time = System.currentTimeMillis();
      PackManager.v().runPacks();
      //output jimple
	  if(outputJimple)
		  {Options.v().set_output_format(1);
		  PackManager.v().writeOutput();
		  }
      t =  ((System.currentTimeMillis() - time)/ 1000f);
      stars = Util.mkStars(t);
      System.out.println("*****************************" + stars); 
      System.out.println("* Finished Analysis " + "[" + t + "s] *");
      System.out.println("*****************************" + stars + "\n"); 

      sharingAnalyzer.reportSharedVariables();
      
      System.out.println("*****************************" + stars);
      System.out.println("*****************************" + stars + "\n"); 

  }

  private static void setClassPath()
  {
      Scene.v().setSootClassPath(System.getProperty("sun.boot.class.path")
              + File.pathSeparator + System.getProperty("java.class.path"));

  }
  private static void setOptions(String dir)
  {
	  if(includeLibrary)
	  Options.v().set_include(Util.includeList);
	  
	  Options.v().set_exclude(Util.excludeList);
	  
	  //FIXME: just for testing
	  //Options.v().set_include_all(true);
	  
      Options.v().set_output_dir(getTempSubDirectory(dir));
      //-------------------
      
      Options.v().set_keep_line_number(true);
      Options.v().set_whole_program(true);
      
      if(!includeLibrary)
      Options.v().set_no_bodies_for_excluded(true);//must be disabled for a sound call graph
      Options.v().set_allow_phantom_refs(true);
      Options.v().set_app(true);
      
//      if(DacapoAnalysis)
//      Options.v().setPhaseOption("cg", "reflection-log:/users/jeff/work/workspace/dacapo-9.12/out/refl.log");
      
      try {
		G.v().out = new PrintStream(new FileOutputStream(Options.v().output_dir() + File.separator + "log"));
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

  }
  private static void enableSpark()
  {
	    //Enable Spark
      HashMap<String,String> opt = new HashMap<String,String>();
      //opt.put("verbose","true");
      opt.put("propagator","worklist");
      opt.put("simple-edges-bidirectional","false");
      opt.put("on-fly-cg","true");
      opt.put("set-impl","double");
      opt.put("double-set-old","hybrid");
      opt.put("double-set-new","hybrid");
      opt.put("pre_jimplify", "true");
      SparkTransformer.v().transform("",opt);
      PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
  }
  private static String getTempSubDirectory(String name)
  {
	  String tempdir = System.getProperty("user.dir");
	
		String dir = tempdir+System.getProperty("file.separator")+name;
		File dirFile = new File(dir);
		if(!(dirFile.exists()))
			dirFile.mkdir();
		
		return dir;	  
	  
  }
}
