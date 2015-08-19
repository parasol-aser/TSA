import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

//import org.apache.derby.jdbc.AutoloadedDriver;

public class Example3 {

	private int x,y;
	static int z;
	int[] a;
	
	public static void main(String[] args) {
		
		Example3 e = new Example3();
		
		int[] b = new int[2];	
		e.a = b;
		
		Thread t1 = new Thread1(e);
		
		t1.start();
		
		b[0]=1;

	}
	
	static class Thread1 extends Thread
	{
		Example3 o;
		Thread1(Example3 e)
		{
			this.o = e;
		}
		public void run()
		{

					o.a[0]=2;
		
		}
	}
}