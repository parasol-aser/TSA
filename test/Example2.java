import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

//import org.apache.derby.jdbc.AutoloadedDriver;

public class Example2 {

	private int x;

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
		Example2 e = new Example2();
		
		HashSet s = new HashSet<Object>();
		s.add(e);
		s.add(new Object());
		
		//for(int i=0;i<2;i++)
		{
		
		
		m2(s);
		

		
		}
	}
	
	public int hashCode()
	{
		return this.getClass().hashCode()+(x++);
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

		public void run()
		{
			//e.x++;
			for(Object o:e)
			{
				System.out.println(o.hashCode());

			}
			
		}
	}
}