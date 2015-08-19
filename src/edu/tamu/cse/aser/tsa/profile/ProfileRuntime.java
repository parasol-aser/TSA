package edu.tamu.cse.aser.tsa.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ProfileRuntime {

	static HashMap<Long, Integer> threadTidIndexMap;
    public static HashSet<Integer> sharedVariableIds;

    public static HashSet<String> sharedArrayWithIndexsString;
    public static HashMap<String, HashSet<Integer>> arrayWithIndexsStringMap;
    static HashMap<String, Long> writeThreadArrayWithIndexStringMap;
    static HashMap<String, long[]> readThreadArrayWithIndexStringMap;

    public static HashSet<Integer> sharedArrayIds;
    static HashMap<Integer, Long> writeThreadMap;
    static HashMap<Integer, long[]> readThreadMap;
    public static HashMap<Integer, HashSet<Integer>> arrayIdsMap;

    static HashMap<Integer, Long> writeThreadArrayMap;
    static HashMap<Integer, long[]> readThreadArrayMap;

    static ThreadLocal<HashSet<Integer>> threadLocalIDSet;
    static ThreadLocal<HashSet<Integer>> threadLocalIDSet2;


    static HashMap<String,HashSet<Long>> dataWriteThreadsMap;
    static HashMap<String,HashSet<Long>> dataReadThreadsMap;

    
	public static void init() {
		
		sharedVariableIds = new HashSet<Integer>();
        writeThreadMap = new HashMap<Integer, Long>();
        readThreadMap = new HashMap<Integer, long[]>();

        sharedArrayWithIndexsString = new HashSet<String>();
        arrayWithIndexsStringMap = new HashMap<String, HashSet<Integer>>();
        writeThreadArrayWithIndexStringMap = new HashMap<String, Long>();
        readThreadArrayWithIndexStringMap = new HashMap<String, long[]>();

        
        
        sharedArrayIds = new HashSet<Integer>();
        arrayIdsMap = new HashMap<Integer, HashSet<Integer>>();
        writeThreadArrayMap = new HashMap<Integer, Long>();
        readThreadArrayMap = new HashMap<Integer, long[]>();

        
        dataWriteThreadsMap = new HashMap<String,HashSet<Long>>();
        dataReadThreadsMap = new HashMap<String,HashSet<Long>>();
        
        threadLocalIDSet = new ThreadLocal<HashSet<Integer>>() {
            @Override
            protected HashSet<Integer> initialValue() {

                return new HashSet<Integer>();

            }
        };
        threadLocalIDSet2 = new ThreadLocal<HashSet<Integer>>() {
            @Override
            protected HashSet<Integer> initialValue() {

                return new HashSet<Integer>();

            }
        };
        
        
        final long start_time = System.currentTimeMillis();
        //add shutdown hook

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                GlobalStateForInstrumentation.instance.saveSharedVariables(start_time);
            }
        });


	}

	public static void accessLocation(int ID)
	{
		//System.out.println("At Location "+ID+" "+(write?"write":"read")+" memory "+address);//+"_"+index;//array";

	}
	
	//MUST INCLUDE CONSTRUCTORS AS DATA RACE CAN OCCUR THERE 
	
	/**
	 * 
	 * @param ID: program location
	 * @param o: runtime object
	 * @param SID: array index or shared variable signature id
	 * @param write: is write or read
	 */
	public static void access(int ID, Object o, int SID, boolean write)
	{
        // instance-based approach consumes too much memory
		
		//for  field only

         String sig = o==null?"."+SID:System.identityHashCode(o)+"."+SID;
         if (!sharedVariableIds.contains(SID)) {

             long tid = Thread.currentThread().getId();
             
	         if(write)
	         {
	        	 HashSet<Long> wtids = dataWriteThreadsMap.get(sig);
	        	 if(wtids==null)
	        	 {
	        		 wtids = new HashSet<Long>();
	        		 dataWriteThreadsMap.put(sig, wtids);
	        	 }
	        	 
	        	 wtids.add(tid);
	        	 if(wtids.size()>1)sharedVariableIds.add(SID);
	        	 else
	        	 {
	        		 if (dataReadThreadsMap.containsKey(sig)) {
	        			 HashSet<Long> rtids = dataReadThreadsMap.get(sig);
                         if (rtids != null
                                 && (rtids.size()>1 || !rtids.contains(tid))) {
                             sharedVariableIds.add(SID);
                         }
                     }

	        	 }
	         }
	         else//read
	         {
	        	 HashSet<Long> rtids = dataReadThreadsMap.get(sig);
	        	 if(rtids==null)
	        	 {
	        		 rtids = new HashSet<Long>();
	        		 dataReadThreadsMap.put(sig, rtids);
	        	 }
	        	 
	        	 rtids.add(tid);
	        	 
	        	 if (dataWriteThreadsMap.containsKey(sig)) {
        			 HashSet<Long> wtids = dataWriteThreadsMap.get(sig);
                     if (wtids!=null&&!wtids.contains(tid)) {
                         sharedVariableIds.add(SID);
                     }
                 }
	         }
         }
	}
    /**
     * detect shared variables -- two conditions 1. the address is accessed by
     * more than two threads 2. at least one of them is a write
     *
     * @param ID
     *            -- shared variable id
     * @param SID
     *            -- field id
     * @param write
     *            or read
     */
    public static void access(int ID, int SID, final boolean write) {

        {
            if (!threadLocalIDSet.get().contains(ID)) {
                if (threadLocalIDSet2.get().contains(ID))
                    threadLocalIDSet.get().add(ID);
                else
                    threadLocalIDSet2.get().add(ID);

                // o is not used...

                // instance-based approach consumes too much memory

                // String sig =
                // o==null?"."+SID:System.identityHashCode(o)+"."+SID;

                long tid = Thread.currentThread().getId();

                
//                if (Config.instance.verbose) {
//                    String readOrWrite = (write ? " write" : " read");
//                    System.out.println("Thread " + tid + " " + readOrWrite + " variable " + SID);
//                }
                if (!sharedVariableIds.contains(SID)) {
                    if (writeThreadMap.containsKey(SID)) {
                        if (writeThreadMap.get(SID) != tid) {
                            sharedVariableIds.add(SID);
                            return;
                        }
                    }

                    if (write)// write
                    {
                        if (readThreadMap.containsKey(SID)) {
                            long[] readThreads = readThreadMap.get(SID);
                            if (readThreads != null
                                    && (readThreads[0] != tid || (readThreads[1] > 0 && readThreads[1] != tid))) {
                                sharedVariableIds.add(SID);
                                return;
                            }
                        }

                        writeThreadMap.put(SID, tid);
                    } else// read
                    {
                        long[] readThreads = readThreadMap.get(SID);

                        if (readThreads == null) {
                            readThreads = new long[2];
                            readThreads[0] = tid;
                            readThreadMap.put(SID, readThreads);
                        } else {
                            if (readThreads[0] != tid)
                                readThreads[1] = tid;

                        }
                    }
                }
            }
        }
    }
    public static void accessArray(int ID, final Object o, final boolean write) {

        if (!threadLocalIDSet.get().contains(ID)) {
            if (threadLocalIDSet2.get().contains(ID))
                threadLocalIDSet.get().add(ID);
            else
                threadLocalIDSet2.get().add(ID);

            Integer sig = System.identityHashCode(o);

            HashSet<Integer> ids = arrayIdsMap.get(sig);
            if (ids == null) {
                ids = new HashSet<Integer>();
                arrayIdsMap.put(sig, ids);
            }
            ids.add(ID);
            long tid = Thread.currentThread().getId();

            
//            if (Config.instance.verbose) {
//                String readOrWrite = (write ? " write" : " read");
//                System.out.println("Thread " + tid + " " + readOrWrite + " array "
//                        + GlobalStateForInstrumentation.instance.getArrayLocationSig(ID));
//            }
            if (!sharedArrayIds.contains(sig)) {
                if (writeThreadArrayMap.containsKey(sig)) {
                    if (writeThreadArrayMap.get(sig) != tid) {
                        sharedArrayIds.add(sig);
                        return;
                    }
                }

                if (write)// write
                {
                    if (readThreadArrayMap.containsKey(sig)) {
                        long[] readThreads = readThreadArrayMap.get(sig);
                        if (readThreads != null
                                && (readThreads[0] != tid || (readThreads[1] > 0 && readThreads[1] != tid))) {
                            sharedArrayIds.add(sig);
                            return;
                        }
                    }

                    writeThreadArrayMap.put(sig, tid);
                } else// read
                {
                    long[] readThreads = readThreadArrayMap.get(sig);

                    if (readThreads == null) {
                        readThreads = new long[2];
                        readThreads[0] = tid;
                        readThreadArrayMap.put(sig, readThreads);
                    } else {
                        if (readThreads[0] != tid)
                            readThreads[1] = tid;

                    }
                }
            }
        }
    }
    
    //for array only 
    public static void accessArray(int ID, final Object o, int index, final boolean write) {

    	
    	
        String sig = System.identityHashCode(o) +"_"+index;//array

    	//System.out.println("Array ACCESS: "+sig);

        HashSet<Integer> ids = arrayWithIndexsStringMap.get(sig);
        if (ids == null) {
            ids = new HashSet<Integer>();
            arrayWithIndexsStringMap.put(sig, ids);
        }
        ids.add(ID);
        
            if (!sharedArrayWithIndexsString.contains(sig)) {

            
                long tid = Thread.currentThread().getId();


                if (writeThreadArrayWithIndexStringMap.containsKey(sig)) {
                	if(sig!=null)
                    if (writeThreadArrayWithIndexStringMap.get(sig) != tid) {
                    	sharedArrayWithIndexsString.add(sig);
                        return;
                    }
                }

                if (write)// write
                {
                    if (readThreadArrayWithIndexStringMap.containsKey(sig)) {
                        long[] readThreads = readThreadArrayWithIndexStringMap.get(sig);
                        if (readThreads != null
                                && (readThreads[0] != tid || (readThreads[1] > 0 && readThreads[1] != tid))) {
                        	sharedArrayWithIndexsString.add(sig);
                            return;
                        }
                    }

                    writeThreadArrayWithIndexStringMap.put(sig, tid);
                } else// read
                {
                    long[] readThreads = readThreadArrayWithIndexStringMap.get(sig);

                    if (readThreads == null) {
                        readThreads = new long[2];
                        readThreads[0] = tid;
                        readThreadArrayWithIndexStringMap.put(sig, readThreads);
                    } else {
                        if (readThreads[0] != tid)
                            readThreads[1] = tid;

                    }
                }
        }
    }
	private static boolean isPrim(Object o) {
		if (o instanceof Integer || o instanceof Long || o instanceof Byte
				|| o instanceof Boolean || o instanceof Float
				|| o instanceof Double || o instanceof Short
				|| o instanceof Character)
			return true;

		return false;
	}
}
