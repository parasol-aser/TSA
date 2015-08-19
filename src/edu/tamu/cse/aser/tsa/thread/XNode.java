package edu.tamu.cse.aser.tsa.thread;

import java.util.HashMap;

public class XNode {

	HashMap<Integer,XDataSharing> fields = new HashMap<Integer,XDataSharing> (1);

	public void accessArray(Byte tid, boolean write) {
	
		XDataSharing xds = fields.get(0);
		if(xds ==null)
		{
			xds = new XDataSharing();
			fields.put(0, xds);
		}
		xds.access(tid, write);
		
	}
	
	public void accessField(Byte tid, Integer fid, boolean write) {
	
		XDataSharing xds = fields.get(fid);
		if(xds ==null)
		{
			xds = new XDataSharing();
			fields.put(fid, xds);
		}
		xds.access(tid, write);
	}
	
	public boolean isArrayShared()
	{
		XDataSharing xds = fields.get(0);
		if(xds==null)
			return false;
		return xds.isShared();
	}
	
	public boolean isFieldShared(Integer fid)
	{
		XDataSharing xds = fields.get(fid);
		if(xds==null)
			return false;
		return xds.isShared();
	}
}
