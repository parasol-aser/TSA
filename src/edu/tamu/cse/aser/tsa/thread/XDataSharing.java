package edu.tamu.cse.aser.tsa.thread;

import java.util.HashSet;

public class XDataSharing {

	private HashSet<Byte> readThreads = new HashSet<Byte>(2);
	private HashSet<Byte> writeThreads = new HashSet<Byte>(2);

	private boolean isShared;
	
	public void access(Byte tid, boolean write)
	{
		if(!isShared)
		{
			if(write)
			{
				if(writeThreads.size()>0)
				{
					if(!writeThreads.contains(tid)) isShared = true;
				}
				else
				{
					if(readThreads.isEmpty()||readThreads.size()==1&&readThreads.contains(tid))							
						writeThreads.add(tid);
					else
						isShared = true;
						
				}
			}
			else
			{
				if(writeThreads.isEmpty()||writeThreads.size()==1&&writeThreads.contains(tid))
					readThreads.add(tid);
				else
					isShared = true;
			}
		}
	}
	public boolean isShared()
	{
		return isShared;
	}
}
