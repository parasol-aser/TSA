package edu.tamu.cse.aser.tsa.profile;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ProfileMethodTransformer extends MethodVisitor implements Opcodes {

	final static String CLASS_INTEGER = "java/lang/Integer";
	final static String CLASS_BOOLEAN = "java/lang/Boolean";
	final static String CLASS_CHAR = "java/lang/Character";
	final static String CLASS_SHORT = "java/lang/Short";
	final static String CLASS_BYTE = "java/lang/Byte";
	final static String CLASS_LONG = "java/lang/Long";
	final static String CLASS_FLOAT = "java/lang/Float";
	final static String CLASS_DOUBLE = "java/lang/Double";

	final static String METHOD_VALUEOF = "valueOf";
	final static String DESC_INTEGER_VALUEOF = "(I)Ljava/lang/Integer;";
	final static String DESC_BOOLEAN_VALUEOF = "(Z)Ljava/lang/Boolean;";
	final static String DESC_BYTE_VALUEOF = "(B)Ljava/lang/Byte;";
	final static String DESC_SHORT_VALUEOF = "(S)Ljava/lang/Short;";
	final static String DESC_CHAR_VALUEOF = "(C)Ljava/lang/Character;";
	final static String DESC_LONG_VALUEOF = "(J)Ljava/lang/Long;";
	final static String DESC_FLOAT_VALUEOF = "(F)Ljava/lang/Float;";
	final static String DESC_DOUBLE_VALUEOF = "(D)Ljava/lang/Double;";

    boolean isInit, isSynchronized, isStatic, staticLock;
	String methodSignature;
	String className;
	   String methodName;

	private int maxindex_cur;// current max index of local variables
	private int line_cur;
	private final GlobalStateForInstrumentation globalState;
	public ProfileMethodTransformer(int access, String desc,
			String methodName, String className, MethodVisitor mv,GlobalStateForInstrumentation globalState) {

        super(Opcodes.ASM5, mv);

		this.methodSignature = methodName + desc;
		this.isInit = (methodName.equals("<init>") || methodName
				.equals("<clinit>"));

        this.maxindex_cur = Type.getArgumentsAndReturnSizes(desc) + 1;
        this.className = className;
        this.methodName = methodName;


        this.globalState = globalState;
	}
	//@Override
    public void visitLineNumber(int line, Label start) {
        line_cur = line;
        mv.visitLineNumber(line, start);
    }
    private void storeValue(String desc, int index)
    {
    	if(desc.startsWith("L")||desc.startsWith("["))
    	{
        	mv.visitInsn(DUP);
    		mv.visitVarInsn(ASTORE, index);
    	}
    	else if (desc.startsWith("I")||desc.startsWith("B")||desc.startsWith("S")||desc.startsWith("Z")||desc.startsWith("C"))
    	{
        	mv.visitInsn(DUP);
    		mv.visitVarInsn(ISTORE, index);
    	}
    	else if (desc.startsWith("J"))
    	{
        	mv.visitInsn(DUP2);
    		mv.visitVarInsn(LSTORE, index);
    		maxindex_cur++;
    	}
    	else if (desc.startsWith("F"))
    	{
    		mv.visitInsn(DUP);
    		mv.visitVarInsn(FSTORE, index);
    	}
    	else if (desc.startsWith("D"))
    	{
        	mv.visitInsn(DUP2);
    		mv.visitVarInsn(DSTORE, index);
    		maxindex_cur++;
    	}

    }
    private void loadValue(String desc, int index)
    {
    	if(desc.startsWith("L")||desc.startsWith("["))
        	mv.visitVarInsn(ALOAD, index);
        else if (desc.startsWith("I"))
        {
        		//convert int to object?
        		mv.visitVarInsn(ILOAD, index);
        		mv.visitMethodInsn(INVOKESTATIC, CLASS_INTEGER, METHOD_VALUEOF,
                        DESC_INTEGER_VALUEOF);
        }
        else if (desc.startsWith("B"))
        {
        		//convert int to object?
        		mv.visitVarInsn(ILOAD, index);
        		mv.visitMethodInsn(INVOKESTATIC, CLASS_BYTE, METHOD_VALUEOF,
                        DESC_BYTE_VALUEOF);
        }
        else if (desc.startsWith("S"))
        {
        		//convert int to object?
        		mv.visitVarInsn(ILOAD, index);
        		mv.visitMethodInsn(INVOKESTATIC, CLASS_SHORT, METHOD_VALUEOF,
                        DESC_SHORT_VALUEOF);
        }
        else if (desc.startsWith("Z"))
        {
        		//convert int to object?
        		mv.visitVarInsn(ILOAD, index);
        		mv.visitMethodInsn(INVOKESTATIC, CLASS_BOOLEAN, METHOD_VALUEOF,
                        DESC_BOOLEAN_VALUEOF);
        }
        else if (desc.startsWith("C"))
        {
        		//convert int to object?
        		mv.visitVarInsn(ILOAD, index);
        		mv.visitMethodInsn(INVOKESTATIC, CLASS_CHAR, METHOD_VALUEOF,
                        DESC_CHAR_VALUEOF);
        }
        else if (desc.startsWith("J"))
        {
    		//convert int to object?
    		mv.visitVarInsn(LLOAD, index);
    		mv.visitMethodInsn(INVOKESTATIC, CLASS_LONG, METHOD_VALUEOF,
                    DESC_LONG_VALUEOF);
        }
    	else if (desc.startsWith("F"))
        {
    		//convert int to object?
    		mv.visitVarInsn(FLOAD, index);
    		mv.visitMethodInsn(INVOKESTATIC, CLASS_FLOAT, METHOD_VALUEOF,
                    DESC_FLOAT_VALUEOF);
        }
    	else if (desc.startsWith("D"))
        {
    		//convert int to object?
    		mv.visitVarInsn(DLOAD, index);
    		mv.visitMethodInsn(INVOKESTATIC, CLASS_DOUBLE, METHOD_VALUEOF,
                    DESC_DOUBLE_VALUEOF);
        }
    	
    }
    private void addInvokeMethod()
    {
    	mv.visitMethodInsn(INVOKESTATIC, Config.instance.logClass, Config.instance.ACCESS,
    			Config.instance.DESC_ACCESS);
    }
    /*
     * NO VALUE IS NEEDED
     * (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void visitFieldInsn2(int opcode, String owner, String name, String desc) {
		String sig_var = (owner + "." + name).replace("/", ".");
		 String sig_loc = getLocSig();

		int SID = globalState
				.getVariableId(sig_var);
		int ID = globalState
				.getFieldLocationId(sig_loc,SID);
//		int ID = globalState
//		.getLocationId(sig_loc);
        switch (opcode) {
        case GETSTATIC:
        	
        	addBipushInsn(mv,ID);
        	mv.visitInsn(ACONST_NULL);
        	addBipushInsn(mv,SID);
        	addBipushInsn(mv,0);
        	

            break;
        case PUTSTATIC:
        	 addBipushInsn(mv,ID);
         	mv.visitInsn(ACONST_NULL);
         	addBipushInsn(mv,SID);         	
             	addBipushInsn(mv,1);
         	
            break;
        case GETFIELD:

        	maxindex_cur++;
        	int index1 = maxindex_cur;
        	mv.visitInsn(DUP);
        	mv.visitVarInsn(ASTORE, index1);
                	
         	addBipushInsn(mv,ID);
         	mv.visitVarInsn(ALOAD, index1);
         	addBipushInsn(mv,SID);
         	
         	addBipushInsn(mv,0);
         	
            break;
        case PUTFIELD:
            // TODO(YilongL): why don't we instrument inner class fields?
            if (name.startsWith("this$")||
            		(className.contains("$") && name.startsWith("val$"))) { // inner class
                mv.visitFieldInsn(opcode, owner, name, desc);

                return;
            }

            maxindex_cur++;
        	index1 = maxindex_cur;     
        	int index2;
        	if(desc.startsWith("D"))
        	{
        		mv.visitVarInsn(DSTORE, index1);
        		maxindex_cur++;//double
        		maxindex_cur++;
            	index2 = maxindex_cur;
            	mv.visitInsn(DUP);
             	mv.visitVarInsn(ASTORE, index2);
             	mv.visitVarInsn(DLOAD, index1);
        	}
        	else if(desc.startsWith("J"))
        	{
        		mv.visitVarInsn(LSTORE, index1);
        		maxindex_cur++;//long
        		maxindex_cur++;
            	index2 = maxindex_cur;
            	mv.visitInsn(DUP);
             	mv.visitVarInsn(ASTORE, index2);
             	mv.visitVarInsn(LLOAD, index1);
        	}
        	else if(desc.startsWith("F"))
        	{
        		mv.visitVarInsn(FSTORE, index1);
        		maxindex_cur++;//float
            	index2 = maxindex_cur;
            	mv.visitInsn(DUP);
             	mv.visitVarInsn(ASTORE, index2);
             	mv.visitVarInsn(FLOAD, index1);
        	}
        	else if(desc.startsWith("["))
        	{
        		mv.visitVarInsn(ASTORE, index1);
        		maxindex_cur++;//ref or array
            	index2 = maxindex_cur;
            	mv.visitInsn(DUP);
             	mv.visitVarInsn(ASTORE, index2);
             	mv.visitVarInsn(ALOAD, index1);
        	}
        	else if(desc.startsWith("L"))
        	{
        		mv.visitVarInsn(ASTORE, index1);
        		maxindex_cur++;//ref or array
            	index2 = maxindex_cur;
            	mv.visitInsn(DUP);
             	mv.visitVarInsn(ASTORE, index2);
             	mv.visitVarInsn(ALOAD, index1);
        	}
        	else
        	{
        		mv.visitVarInsn(ISTORE, index1);
        		maxindex_cur++;//integer,char,short,boolean
            	index2 = maxindex_cur;
            	mv.visitInsn(DUP);
             	mv.visitVarInsn(ASTORE, index2);
             	mv.visitVarInsn(ILOAD, index1);
        	}        	 
        	
        	addBipushInsn(mv,ID);
          	mv.visitVarInsn(ALOAD, index2);
          	addBipushInsn(mv,SID);
              	addBipushInsn(mv,1);
         	
            break;
        default:
            System.err.println("Unknown field access opcode " + opcode);
            System.exit(1);
        }
        
    	addInvokeMethod();

        mv.visitFieldInsn(opcode, owner, name, desc);

    }
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		if(Config.instance.fastSharingAnalysis)
			visitFieldInsn1( opcode,  owner,  name, desc);
		else visitFieldInsn2( opcode,  owner,  name, desc);
			
	}
	  private String getLocSig()
	  {
		  String sig = 
		  (className + "|" + methodName + "|" + line_cur)
	              .replace("/", ".");
		  //System.out.println(sig);
		  return sig;
		  
//		  return (className + "|" + methodSignature + "|" + line_cur)
//	    .replace("/", ".");
	  }
    //optimize field
	public void visitFieldInsn1(int opcode, String owner, String name,
			String desc) {

		String sig_var = (owner + "." + name).replace("/", ".");
		 String sig_loc = getLocSig();

		int SID = globalState
				.getVariableId(sig_var);
		int ID = globalState
				.getFieldLocationId(sig_loc,SID);
		

        
    		switch (opcode) {
    		case GETSTATIC:
    		case GETFIELD:
    	        addBipushInsn(mv, ID);
    	        addBipushInsn(mv, SID);

                addBipushInsn(mv, 0);

    			break;


    		case PUTFIELD:
    			if (name.startsWith("this$")||
                		(className.contains("$") && name.startsWith("val$"))) { // inner class
                    mv.visitFieldInsn(opcode, owner, name, desc);

                    return;
                }
    		case PUTSTATIC:
    	        addBipushInsn(mv, ID);
    	        addBipushInsn(mv, SID);
                 addBipushInsn(mv, 1);

    			break;
    		default:
    			System.err.println("Unknown field access opcode " + opcode);
    			System.exit(1);
    		}

			mv.visitMethodInsn(INVOKESTATIC, Config.instance.logClass,
					Config.instance.ACCESS2,
					Config.instance.DESC_ACCESS2);

            mv.visitFieldInsn(opcode, owner, name, desc);

	}


    //@Override
    public void visitMethodInsn2(int opcode, String owner, String name,
            String desc) {         

		 String sig_loc = getLocSig();

        int ID = GlobalStateForInstrumentation.instance
                .getLocationId(sig_loc);
        switch (opcode) {
        case INVOKEINTERFACE:
            if (owner.equals("java/util/Iterator")
                    && name.equals("next") && desc.equals("()Ljava/lang/Object;")) {
                addBipushInsn(mv, ID);

    			mv.visitMethodInsn(INVOKESTATIC, Config.instance.logClass,
    					Config.instance.ACCESS_LOCATION,
    					Config.instance.DESC_ACCESS_LOCATION);

            } break;
        case INVOKEVIRTUAL:
        case INVOKESPECIAL:
        case INVOKESTATIC:

            break;
        default:
            System.err.println("Unknown method invocation opcode " + opcode);
            System.exit(1);
        }
        
        mv.visitMethodInsn(opcode, owner, name, desc);
    }
    public void visitMaxs(int maxStack, int maxLocals) {
        mv.visitMaxs(maxStack + 5, maxindex_cur+2);//may change to ...

    }


    private int arrayStoreOpcode(int opcode)
    {
        switch (opcode) {
        case AASTORE:maxindex_cur++;return ASTORE;
        case FASTORE:maxindex_cur++;return FSTORE;
        case DASTORE:maxindex_cur++;return DSTORE;
        case LASTORE:maxindex_cur++;return LSTORE;
        case BASTORE:
        case CASTORE:
        case SASTORE:
        case IASTORE:
        default: return ISTORE;
        }
    }
	private int arrayLoadOpcode(int opcode)
    {
        switch (opcode) {
        case AASTORE:return ALOAD;
        case FASTORE:return FLOAD;
        case DASTORE:return DLOAD;
        case LASTORE:return LLOAD;
        case BASTORE:
        case CASTORE:
        case SASTORE:
        case IASTORE:
        default: return ILOAD;
        }
    }
    private void instrumentArrayAccess(int opcode, boolean isWrite)
    {
		 String sig_loc = getLocSig();

        int ID = GlobalStateForInstrumentation.instance
                .getArrayLocationId(sig_loc);


        if(isWrite)
        {
            maxindex_cur++;
            int index1 = maxindex_cur;
            mv.visitVarInsn(arrayStoreOpcode(opcode), index1);
            
            maxindex_cur++;
            int index2 = maxindex_cur;
            mv.visitVarInsn(ISTORE, index2);

            mv.visitInsn(DUP);
            maxindex_cur++;
            int index3 = maxindex_cur;
            mv.visitVarInsn(ASTORE, index3);// arrayref


            addBipushInsn(mv, ID);
            mv.visitVarInsn(ALOAD, index3);
            mv.visitVarInsn(ILOAD, index2);

            addBipushInsn(mv, 1);

			mv.visitMethodInsn(INVOKESTATIC, Config.instance.logClass,
					Config.instance.ACCESS_ARRAY,
					Config.instance.DESC_ACCESS_ARRAY);

            mv.visitVarInsn(ILOAD, index2);// index
            mv.visitVarInsn(arrayLoadOpcode(opcode), index1);// value

        }
        else {
            mv.visitInsn(DUP2);
            maxindex_cur++;
            int index1 = maxindex_cur;
            mv.visitVarInsn(ISTORE, index1);
            maxindex_cur++;
            int index2 = maxindex_cur;
            mv.visitVarInsn(ASTORE, index2);


            addBipushInsn(mv, ID);
            mv.visitVarInsn(ALOAD, index2);
            mv.visitVarInsn(ILOAD, index1);

            addBipushInsn(mv, 0);

			mv.visitMethodInsn(INVOKESTATIC, Config.instance.logClass,
					Config.instance.ACCESS_ARRAY,
					Config.instance.DESC_ACCESS_ARRAY);
    }
    }
    private void instrumentArrayAccess2(int opcode, boolean isWrite)
    {
		 String sig_loc = getLocSig();

        int ID = GlobalStateForInstrumentation.instance
                .getArrayLocationId(sig_loc);


        if(isWrite)
        {
            maxindex_cur++;
            int index1 = maxindex_cur;
            mv.visitVarInsn(arrayStoreOpcode(opcode), index1);
            
            maxindex_cur++;
            int index2 = maxindex_cur;
            mv.visitVarInsn(ISTORE, index2);

            mv.visitInsn(DUP);
            maxindex_cur++;
            int index3 = maxindex_cur;
            mv.visitVarInsn(ASTORE, index3);// arrayref


            addBipushInsn(mv, ID);
            mv.visitVarInsn(ALOAD, index3);

            addBipushInsn(mv, 1);

			mv.visitMethodInsn(INVOKESTATIC, Config.instance.logClass,
					Config.instance.ACCESS_ARRAY2,
					Config.instance.DESC_ACCESS_ARRAY2);

            mv.visitVarInsn(ILOAD, index2);// index
            mv.visitVarInsn(arrayLoadOpcode(opcode), index1);// value

        }
        else {
            mv.visitInsn(DUP2);
            maxindex_cur++;
            int index1 = maxindex_cur;
            mv.visitVarInsn(ISTORE, index1);
            maxindex_cur++;
            int index2 = maxindex_cur;
            mv.visitVarInsn(ASTORE, index2);


            addBipushInsn(mv, ID);
            mv.visitVarInsn(ALOAD, index2);

            addBipushInsn(mv, 0);

			mv.visitMethodInsn(INVOKESTATIC, Config.instance.logClass,
					Config.instance.ACCESS_ARRAY2,
					Config.instance.DESC_ACCESS_ARRAY2);
    }
    }
    public void visitJumpInsn2(int opcode, Label label) {
		 String sig_loc = getLocSig();

        int ID = GlobalStateForInstrumentation.instance
                .getLocationId(sig_loc);

        switch (opcode) {
        case IFEQ:// branch
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case IFNULL:
        case IFNONNULL:
//            addBipushInsn(mv, ID);
//            mv.visitMethodInsn(INVOKESTATIC, RVInstrumentor.logClass,
//                    RVConfig.instance.LOG_BRANCH,
//                    RVConfig.instance.DESC_LOG_BRANCH);
        default:
            mv.visitJumpInsn(opcode, label);
            break;
        }
    }

    public void visitIincInsn2(int var, int increment) {
        if (var > maxindex_cur) {
            maxindex_cur = var;
        }
        mv.visitIincInsn(var, increment);
    }
    public void visitVarInsn(int opcode, int var) {
        if (var > maxindex_cur) {
            maxindex_cur = var;
        }

        switch (opcode) {
        case LSTORE:
        case DSTORE:
        case LLOAD:
        case DLOAD:
            if (var == maxindex_cur) {
                maxindex_cur = var + 1;
            }
            mv.visitVarInsn(opcode, var);
            break;
        case ISTORE:
        case FSTORE:
        case ASTORE:
        case ILOAD:
        case FLOAD:
        case ALOAD:
        case RET:
            mv.visitVarInsn(opcode, var);
            break;
        default:
            System.err.println("Unknown var instruction opcode " + opcode);
            System.exit(1);
        }
    }
    
    public void visitInsn(int opcode) {
    	
		if(Config.instance.fastSharingAnalysis)
			visitInsn1(opcode);
    	else visitInsn2(opcode);
    }
	public void visitInsn1(int opcode) {

		// Array access here
		if (opcode == Opcodes.AALOAD || opcode == Opcodes.IALOAD
				|| opcode == Opcodes.LALOAD || opcode == Opcodes.SALOAD
				|| opcode == Opcodes.CALOAD || opcode == Opcodes.DALOAD
				|| opcode == Opcodes.FALOAD || opcode == Opcodes.BALOAD) {

            instrumentArrayAccess2(opcode,false);


		} else if (opcode == Opcodes.AASTORE || opcode == Opcodes.IASTORE
				|| opcode == Opcodes.LASTORE || opcode == Opcodes.SASTORE
				|| opcode == Opcodes.CASTORE || opcode == Opcodes.DASTORE
				|| opcode == Opcodes.FASTORE || opcode == Opcodes.BASTORE) {

            instrumentArrayAccess2(opcode,true);

		}

     mv.visitInsn(opcode);

}
	public void visitInsn2(int opcode) {

    		// Array access here
    		if (opcode == Opcodes.AALOAD || opcode == Opcodes.IALOAD
    				|| opcode == Opcodes.LALOAD || opcode == Opcodes.SALOAD
    				|| opcode == Opcodes.CALOAD || opcode == Opcodes.DALOAD
    				|| opcode == Opcodes.FALOAD || opcode == Opcodes.BALOAD) {

                instrumentArrayAccess(opcode,false);


    		} else if (opcode == Opcodes.AASTORE || opcode == Opcodes.IASTORE
    				|| opcode == Opcodes.LASTORE || opcode == Opcodes.SASTORE
    				|| opcode == Opcodes.CASTORE || opcode == Opcodes.DASTORE
    				|| opcode == Opcodes.FASTORE || opcode == Opcodes.BASTORE) {

                instrumentArrayAccess(opcode,true);

    		}

         mv.visitInsn(opcode);

	}

	private void addBipushInsn(MethodVisitor mv, int val) {
		switch (val) {
		case 0:
			mv.visitInsn(ICONST_0);
			break;
		case 1:
			mv.visitInsn(ICONST_1);
			break;
		case 2:
			mv.visitInsn(ICONST_2);
			break;
		case 3:
			mv.visitInsn(ICONST_3);
			break;
		case 4:
			mv.visitInsn(ICONST_4);
			break;
		case 5:
			mv.visitInsn(ICONST_5);
			break;
		default:
			mv.visitLdcInsn(new Integer(val));
			break;
		}
	}
}
