package edu.tamu.cse.aser.tsa.profile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public static final Config instance = new Config();
    public static final String propFile = "tsa.conf";

    public final String ACCESS = "access";
    public final String DESC_ACCESS = "(ILjava/lang/Object;IZ)V";

    public final String ACCESS2 = "access";
    public final String DESC_ACCESS2 = "(IIZ)V";
    
    public final String ACCESS_ARRAY = "accessArray";
    public final String DESC_ACCESS_ARRAY = "(ILjava/lang/Object;IZ)V";
    
    public final String ACCESS_ARRAY2 = "accessArray";
    public final String DESC_ACCESS_ARRAY2 = "(ILjava/lang/Object;Z)V";
    
    public final String ACCESS_LOCATION = "accessLocation";
    public final String DESC_ACCESS_LOCATION = "(I)V";
    
    public final String LOG_FIELD_ACCESS = "logFieldAcc";
    public final String LOG_INIT_WRITE_ACCESS = "logInitialWrite";
    public final String LOG_ARRAY_ACCESS = "logArrayAcc";
    public final String LOG_LOCK_INSTANCE = "logLock";
    public final String LOG_LOCK_STATIC = "logStaticSyncLock";
    public final String LOG_UNLOCK_INSTANCE = "logUnlock";
    public final String LOG_UNLOCK_STATIC = "logStaticSyncUnlock";
    public final String LOG_BRANCH = "logBranch";
//    public final String LOG_THREAD_START = "logStart";
    public final String LOG_THREAD_JOIN = "logJoin";
    public final String LOG_THREAD_SLEEP = "logSleep";
    public final String LOG_WAIT = "logWait";
    public final String LOG_NOTIFY = "logNotify";
    public final String LOG_NOTIFY_ALL = "logNotifyAll";

    public final String LOG_THREAD_BEFORE_START = "logBeforeStart";
    public final String LOG_THREAD_AFTER_START = "logAfterStart";
    public final String LOG_THREAD_BEGIN = "logThreadBegin";
    public final String LOG_THREAD_END = "logThreadEnd";

    public final String DESC_LOG_FIELD_ACCESS = "(ILjava/lang/Object;ILjava/lang/Object;Z)V";
    public final String DESC_LOG_ARRAY_ACCESS_DETECT_SHARING ="(ILjava/lang/Object;IZ)V";
    public final String DESC_LOG_FIELD_ACCESS_DETECT_SHARING = "(IIZ)V";

    public final String DESC_LOG_INIT_WRITE_ACCESS = "(ILjava/lang/Object;ILjava/lang/Object;)V";
    public final String DESC_LOG_ARRAY_ACCESS = "(ILjava/lang/Object;ILjava/lang/Object;Z)V";
    public final String DESC_LOG_LOCK_INSTANCE = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_LOCK_STATIC = "(II)V";
    public final String DESC_LOG_UNLOCK_INSTANCE = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_UNLOCK_STATIC = "(II)V";
    public final String DESC_LOG_BRANCH = "(I)V";
    public final String DESC_LOG_THREAD_START = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_THREAD_JOIN = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_THREAD_SLEEP = "()V";
    public final String DESC_LOG_WAIT = "(ILjava/lang/Object;)V";
    public final String DESC_LOG_NOTIFY = "(ILjava/lang/Object;)V";

    public final String DESC_LOG_THREAD_BEGIN = "()V";
    public final String DESC_LOG_THREAD_END = "()V";

    public boolean verbose = true;

    public boolean fastSharingAnalysis;
    
    public String[] excludeList;
    public String[] includeList;

    public String logClass;// = "edu/tamu/cse/aser/profile/ProfileRunTime";

    public String entryMethod;
    public Config() {
    	Properties properties = new Properties();
        logClass = properties.getProperty("tsa.logClass", "edu.tamu.cse.aser.tsa.profile.ProfileRuntime").replace(
                '.', '/');
        try {

            properties.load(ClassLoader.getSystemClassLoader()// this.getClass().getClassLoader()
                    .getResourceAsStream(propFile));

            verbose = properties.getProperty("tsa.verbose", "false").equals("true");
            
            fastSharingAnalysis = properties.getProperty("tsa.fastSharingAnalysis", "false").equals("true");

            
            excludeList = properties.getProperty("tsa.excludeList", "").split(",");
            if (excludeList.length == 1 && excludeList[0].isEmpty()) {
                excludeList = null;
            }
            includeList = properties.getProperty("tsa.includeList", "").split(",");
            if (includeList.length == 1 && includeList[0].isEmpty()) {
                includeList = null;
            }

            entryMethod = properties.getProperty("tsa.entry", null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
