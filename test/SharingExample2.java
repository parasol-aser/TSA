public class SharingExample2 {

	private static int x, y;
	private static Object lock = new Object();
	
	//test without a main method
	public static void test() {
		
		
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < 2; i++) {
					synchronized (lock) {
						x = 0;
					}
					if (x > 0) {
						y++;
						x = 2;
					}
				}
			}

		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 2; i++) {
					if (x > 1) {
						if (y == 3) {
							System.out.println("Crash");
						} else
							y = 2;
					}
				}
			}

		});
		t1.start();
		t2.start();

		for (int i = 0; i < 2; i++) {
			synchronized (lock) {
				x = 1;
//				for (int j = 0; j < 8; j++) {
					y = 1;
//				}
			}
		}
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}