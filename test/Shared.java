public class Shared {
	
	int x=1;
	int y=1;
	static int z=1;
	int a[] = new int[1];
	@Override
	public int hashCode()
	{
		return y+x++;
	}
}	
