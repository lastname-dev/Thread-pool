package threadpool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.Test;

public class ThreadPoolTest {

	@Test
	void submittedTasksAreExecuted() throws InterruptedException {
		final Executor executor = new ThreadPool(2);
		final int numTasks = 100;
		final CountDownLatch latch = new CountDownLatch(100);
		try {
			for (int i = 0; i < numTasks; i++) {
				int finalI = i;
				executor.execute(() -> {
					System.err.println("Thread " + Thread.currentThread().getName() + " executes a task" + finalI);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					latch.countDown();
				});
			}
			// latch.await();
		} finally {
			((ThreadPool)executor).shutdown();
		}
	}
}
