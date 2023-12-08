package threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool implements Executor {

	private final BlockingQueue<Runnable> queue = new LinkedTransferQueue<>();
	private final Thread[] threads;
	private final AtomicBoolean started = new AtomicBoolean();
	private boolean shutdown;

	public ThreadPool(int numThreads) {
		threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Thread(() -> {
				while (!shutdown) {
					Runnable task = null;
					try {
						task = queue.take();
					} catch (InterruptedException e) {
					}
					if(task != null) {
						try {
							task.run();
						}catch (Throwable t){
							System.out.println("Exception: ");
							t.printStackTrace();
						}
					}
				}
				System.err.println("Shutting thread :"+ Thread.currentThread().getName() );
			});
		}

	}

	@Override
	public void execute(Runnable command) {
		if (started.compareAndSet(false, true)) {
			for (Thread thread : threads) {
				thread.start();
			}
		}
		queue.add(command);
	}
	public void shutdown() {
		shutdown = true;
		for (Thread thread : threads){
			thread.interrupt();
		}

		for (Thread thread : threads) {
			while (thread.isAlive()) {
				thread.interrupt();
				try {
					thread.join();
				} catch (InterruptedException e) {

				}
			}
		}
	}
}
