import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

//import org.apache.derby.jdbc.AutoloadedDriver;

public class Example1 {

	private int x;
	private int[] a = new int[2];
	private int[] b = new int[2];

	public static void main(String[] args) {
		
//		CopyOnWriteArrayList<Driver> registeredDrivers = new CopyOnWriteArrayList<Driver>();
//		registeredDrivers.addIfAbsent(new AutoloadedDriver());
//		 for(Driver driver : registeredDrivers) {
//        try {
//			Connection con = driver.connect("jdbc:derby:DERBY2861;create=true", new java.util.Properties());
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		 }
		Example1 e = new Example1();
		
		HashSet s = new HashSet<Object>();
		s.add(e);
		s.add(new Object());
		
		//for(int i=0;i<2;i++)
		{

		e.a[0]=1;
		e.b[1]=2;
		
		m(e);
		
		main2(s);
		
		m2(s);
		
		e.a[1]=2;

		
		}
	}
	
	public int hashCode()
	{
		return this.getClass().hashCode()+(x++);
	}
	
	static void main2(HashSet e)
	{
		System.out.println("abc");
		
		m2(e);
		
	}
	static void m(Example1 e)
	{
		e.x++;

	}
	static void m2(HashSet e)
	{
Thread t1 = new Thread1(e);

		
		t1.start();
		
		try {
			t1.join();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

	}
	static class Thread1 extends Thread
	{
		HashSet e;
		Thread1(HashSet e)
		{
			this.e = e;
		}
		public void test()
		{
			int[] a = new int[2];
			a[0] = 1;
			Object b = new Object();
			//a =b;
		}
		public void run()
		{
			//e.x++;
			for(Object o:e)
			{
				System.out.println(o.hashCode());
				if(o instanceof Example1)
				{
					((Example1) o).a[0]=2;
				}
			}
			
			test();
		}
	}
}