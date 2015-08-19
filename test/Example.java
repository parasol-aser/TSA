import java.util.HashSet;
import java.util.Set;
public class Example {
	public static void main(String[] args) {
		Shared s = new Shared();
		HashSet set = new HashSet<Object>();
		set.add(s);
		Thread t2 = new Thread2(set);
		Thread t3 = new Thread3(s);		
		t2.start();
		t3.start();
		s.x=1;
		s.z=1;
		int[] b = s.a;
		b[0]=1;
	}
	static class Thread2 extends Thread{
		Set set;
		public Thread2(HashSet set) {
			this.set = set;
		}
		public void run(){
			for(Object o:set)
				System.out.println(o.hashCode());
		}
	}
	static class Thread3 extends Thread{
		Shared s;
		public Thread3(Shared s) {
			this.s = s;
		}
		public void run(){
			s.a[0]=2;
		}
	}
}