package edu.tamu.cse.aser.tsa.profile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;

public class GlobalStateForInstrumentation {


    public final static String FILE_SHARED_VARIABLE = "sharedvariables.ser";
    public final static String FILE_SHARED_ARRAY_LOCATION = "sharedarrayloc.ser";

    public static final String RUNNABLE_CLASS_NAME = "java/lang/Runnable";
    public static final String OBJECT_CLASS_NAME = "java/lang/Object";
    public static final String THREAD_CLASS_NAME = "java/lang/Thread";


    public static GlobalStateForInstrumentation instance = new GlobalStateForInstrumentation();
	public HashMap<Integer, String> variableIdSigMap = new HashMap<Integer, String>();
	public HashMap<Integer, String> stmtIdSigMap = new HashMap<Integer, String>();
	public HashSet<String> volatilevariables = new HashSet<String>();

	    public ConcurrentHashMap<String,Integer> variableIdMap = new ConcurrentHashMap<String,Integer>();
	    //public ConcurrentHashMap<String,Integer> unsavedVariableIdMap = new ConcurrentHashMap<String,Integer>();
	    public HashMap<Integer,String> arrayIdMap = new HashMap<Integer,String>();

	    public HashSet<String> volatileVariables = new HashSet<String>();
	    //public ConcurrentHashMap<String,Boolean> unsavedVolatileVariables = new ConcurrentHashMap<>();
	    public ConcurrentHashMap<String,Integer> stmtSigIdMap = new ConcurrentHashMap<String,Integer>();
	    //public ConcurrentHashMap<String,Integer> unsavedStmtSigIdMap = new ConcurrentHashMap<String,Integer>();
	    HashSet<String> sharedVariables;
	    HashSet<String> sharedArrayLocations;
	    HashSet<String> sharedFieldLocations;

	    HashMap<Integer,HashSet<Integer>> variableIdLocationIdsMap = new HashMap<Integer,HashSet<Integer>>();
	    
	    
	    int totalAccesses;
	    int sharedAccesses;
	    
	    public void saveObjectToFile(Object o, String filename)
	    {
	        // save the object to file
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            try {
              fos = new FileOutputStream(filename);
              out = new ObjectOutputStream(fos);
              out.writeObject(o);

              out.close();
            } catch (Exception ex) {
              ex.printStackTrace();
            }


	    }
		public void saveSharedVariables(long start_time) {
			
	 // show arrayId
			HashSet<Integer> sharedArrayIds = new HashSet<Integer>();
			
	        for (Integer sid : ProfileRuntime.sharedArrayIds) {
	            HashSet<Integer> ids = ProfileRuntime.arrayIdsMap.get(sid);
	            sharedArrayIds.addAll(ids);
	        }
        	sharedAccesses+=sharedArrayIds.size();//shared array accesses

	        
	        HashSet<Integer> sharedArraySet = new HashSet<Integer>();

	        for (String sid : ProfileRuntime.sharedArrayWithIndexsString) {
	        	HashSet<Integer> set = ProfileRuntime.arrayWithIndexsStringMap.get(sid);
	        	if(set!=null)sharedArraySet.addAll(set);
	        }
        	sharedAccesses+=sharedArraySet.size();//shared array accesses

        	
	        sharedArrayLocations = new HashSet<String>();
	    	
	        for (Integer id : arrayIdMap.keySet()) {
	            String var = arrayIdMap.get(id);
	            if (sharedArraySet.contains(id))
	                sharedArrayLocations.add(var);
	        }
	        
	        for (Integer id : arrayIdMap.keySet()) {
            String var = arrayIdMap.get(id);
            if (sharedArrayIds.contains(id))
                sharedArrayLocations.add(var);
	        }        
	        
//	        HashSet<Integer> allArraySet = new HashSet<Integer>();
//	        
//	        for(HashSet<Integer> set : ProfileRuntime.arrayWithIndexsStringMap.values())
//	        	allArraySet.addAll(set);
//	        
//	        totalAccesses+=allArraySet.size();
	        
	        sharedVariables = new HashSet<String>();
	        sharedFieldLocations = new HashSet<String>(); 

	        // show variableId
	        for (Map.Entry<String, Integer> entry : variableIdMap.entrySet()) {
	            Integer id = entry.getValue();
	            String var = entry.getKey();
	            if (ProfileRuntime.sharedVariableIds.contains(id))
	            {
	                sharedVariables.add(var);
	                
	                HashSet<Integer> set = variableIdLocationIdsMap.get(id);
	                if(set!=null)
	                {
	                	sharedAccesses+=set.size();//shared field accesses
	                	for(Integer locID : set)
	                		sharedFieldLocations.add(getStmtSignature(locID));
	                }
	            }
	            
//	            HashSet set = variableIdLocationIdsMap.get(id);
//	            if(set!=null) totalAccesses+=set.size();
	            
	        }
	        


	
	        if (Config.instance.verbose) {
	        	
	            System.out.println(
	                    "****************************************************");
	                System.out.println("* "+sharedVariables.size()+
	                    " Shared Field Variables *");
	                System.out.println(
	                    "****************************************************");
			      	for (String s: sharedVariables) 
			      	{
			      	      System.out.println(s);
			      	}
	        	
			      	
			      	HashSet<String> sharedVariableLocations = new HashSet<String>();
			      	
			      	sharedVariableLocations.addAll(sharedFieldLocations);
			      	sharedVariableLocations.addAll(sharedArrayLocations);
			     // Serialize / save it
				  	  try {
				  		  ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("tmp/sharedLocations"));
				  		oos.writeObject(sharedVariableLocations);
				  		oos.close();
				  	} catch (IOException e) {
				  		// TODO Auto-generated catch block
				  		e.printStackTrace();
				  	}
			  	  
			      	
		            System.out.println(
		                    "****************************************************");
		                System.out.println("* "+sharedFieldLocations.size()+
		                    " Shared Field Access Locations *");
		                System.out.println(
		                    "****************************************************");
				      	for (String s:sharedFieldLocations ) 
				      	{
				      	      System.out.println(s);
				      	}
				      	
		            System.out.println(
		                    "****************************************************");
		                System.out.println("* "+sharedArrayLocations.size()+
		                    " Shared Array Access Locations *");
		                System.out.println(
		                    "****************************************************");
				      	for (String s: sharedArrayLocations) 
				      	{
				      	      System.out.println(s);
				      	}	
			     	
	            int size_var = variableIdMap.entrySet().size();
	            int size_array = arrayIdMap.entrySet().size();
	
	            double svar_percent = size_var == 0 ? 0 : ((double) ProfileRuntime.sharedVariableIds
	                    .size() / variableIdMap.entrySet().size());
	            double sarray_percent = size_array == 0 ? 0
	                    : ((double) (sharedArraySet.size()+sharedArrayIds.size()) / arrayIdMap.entrySet().size());
	
	            System.out.println("\nSHARED VARIABLE PERCENTAGE: " + svar_percent);
	            System.out.println("SHARED ARRAY PERCENTAGE: " + sarray_percent);
	            	       
	            totalAccesses = this.stmtIdSigMap.keySet().size();
	            
	            System.out.println("\nSHARED/TOTAL: " + sharedAccesses+"/"+totalAccesses);
	            System.out.println("\nTotal time: " + (System.currentTimeMillis()-start_time));

	        }
		}
	    public boolean initSharedData()
	    {
	        Object o = loadObjectFromFile(FILE_SHARED_VARIABLE);
	        if(o==null)return false;
	        else sharedVariables = (HashSet<String>) o;

	            o = loadObjectFromFile(FILE_SHARED_ARRAY_LOCATION);
	            if(o==null)return false;
	            else sharedArrayLocations = (HashSet<String>) o;

	            return true;
	    }
	    private Object loadObjectFromFile(String filename)
	    {
	        Object o = null;
	         // read the object from file
            // save the object to file
            FileInputStream fis = null;
            ObjectInputStream in = null;
            try {
              fis = new FileInputStream(filename);
              in = new ObjectInputStream(fis);
               o = in.readObject();
              //System.out.println(o);
              in.close();
            } catch (Exception ex) {
              ex.printStackTrace();
            }

            return o;
	    }
	    public boolean isVariableShared(String sig)
	    {
	        if(true)return true;
	        if(sharedVariables==null
	                ||sharedVariables.contains(sig))
	            return true;
	        else
	            return false;
	    }
	    public boolean shouldInstrumentArray(String loc)
	    {
	           if(true)return true;

	        if(sharedArrayLocations==null
	                ||sharedArrayLocations.contains(loc))
	            return true;
	        else
	            return false;
	    }
	    public void setSharedArrayLocations(HashSet<String> locs)
	    {
	        this.sharedArrayLocations = locs;
	    }
	    public void setSharedVariables(HashSet<String> locs)
	    {
	        this.sharedVariables = locs;
	    }
	    public void computeStats2(long l, HashMap<Integer, ArrayList<Long>> totalTimeListMap) {

	    	//get largest in totalTimeMap
	    	for(Integer ID :totalTimeListMap.keySet())
	    		{
	    		String loc = getStmtSignature(ID);
	    		ArrayList<Long> list = totalTimeListMap.get(ID);
	    		int size = list.size();
	    		if(size>1)
	    		{
	    		System.out.println("At "+loc+": "+totalTimeListMap.get(ID));
	    		double sum=0;
	    		for(int i=0;i<size;i++)
	    			sum+=list.get(i);
	    		
	    			System.out.println("Average time (ms): "+(sum/size));

	    		}
	    		}

	    		
	    }
	    public void computeStats(long l, HashMap<String, Long> totalTimeMap) {


	    	long largest =0;
	    	//get largest in totalTimeMap
	    	for(Long v :totalTimeMap.values())
	    		{
	    			if(v>largest) largest =v;
	    		};

	    		System.out.println("PScore: "+l/((double) largest));
	    }

	    public void addVolatileVariable(String sig)
	    {
	        if (!volatileVariables.contains(sig)) {
	            synchronized (volatileVariables) {
	                if (!volatileVariables.contains(sig)) {
	                    volatileVariables.add(sig);
	                    //unsavedVolatileVariables.put(sig, true);
	                }
	            }
	        }
	    }

	    public String getStmtSignature(int ID)
	    {
	    	return stmtIdSigMap.get(ID);
	    }
	    public int getLocationId(String sig)
	    {
	        if(stmtSigIdMap.get(sig)==null)
	      {
	          synchronized (stmtSigIdMap) {
	              if(stmtSigIdMap.get(sig)==null) {
	                  int size = stmtSigIdMap.size() + 1;
	                  stmtSigIdMap.put(sig, size);
	                  stmtIdSigMap.put(size,sig);
	              }
	          }
	      }

	      return stmtSigIdMap.get(sig);
	    }
	    public int getFieldLocationId(String sig, int SID)
	    {
	      int ID = getLocationId(sig);
	      
	      HashSet<Integer> set = variableIdLocationIdsMap.get(SID);
	      if(set==null)
	      {
	    	  set = new HashSet<Integer>();
	    	  variableIdLocationIdsMap.put(SID, set);
	      }
	      set.add(ID);
	      
	      return ID;
	    }
	    public int getArrayLocationId(String sig)
	    {
	        int id = getLocationId(sig);

	        arrayIdMap.put(id,sig);

	        return id;
	    }
	    public String getArrayLocationSig(int id)
	    {
	        return arrayIdMap.get(id);
	    }

//	public RVGlobalStateForInstrumentation() {
//		// save instrumentation and runtime information?
//		Runtime.getRuntime().addShutdownHook(new Thread("Thread-logMetaData") {
//			public void run() {
//				// RecordRT.saveMetaData(variableIdMap, volatilevariables,
//				// stmtSigIdMap,Config.instance.verbose);
//			}
//		});
//
//	}

	public int getVariableId(String sig) {
		if(variableIdMap.get(sig)==null) {
	          synchronized (variableIdMap) {
	              if (variableIdMap.get(sig) == null) {
	                  int size = variableIdMap.size() + 1;
	                  variableIdMap.put(sig, size);
	                  variableIdSigMap.put(size, sig);
	              }
	          }
	      }
	      int sid = variableIdMap.get(sig);

	      return sid;
	}

    public boolean isThreadClass(String cname) {
        while (!cname.equals(OBJECT_CLASS_NAME)) {
            if (cname.equals(THREAD_CLASS_NAME))
                return true;

            try {
                ClassReader cr = new ClassReader(cname);
                cname = cr.getSuperName();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                // //if class can not find
                // System.out.println("Class "+cname+" can not find!");
                return false;
            }
        }
        return false;
    }
	public boolean isRunnableClass(String cname) {
		while (!cname.equals(OBJECT_CLASS_NAME)) {
//			if (cname.equals("java/lang/Runnable"))
//				return true;

			try {
				ClassReader cr = new ClassReader(cname);

				String[] interfaces =  cr.getInterfaces();
				for(int i=0;i<interfaces.length;i++)
				    if(interfaces[i].equals(RUNNABLE_CLASS_NAME))return true;

				cname = cr.getSuperName();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				// //if class can not find
				// System.out.println("Class "+cname+" can not find!");
				return false;
			}
		}
		return false;
	}

}
