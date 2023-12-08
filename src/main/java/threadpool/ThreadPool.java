package threadpool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool implements Executor {

	private static final Runnable SHUTDOWN_TASK = () -> {
	};
	private final BlockingQueue<Runnable> queue = new LinkedTransferQueue<>();
	private final Set<Thread> threads = new HashSet<>();
	private final AtomicBoolean started = new AtomicBoolean();
	private final AtomicBoolean shutdown = new AtomicBoolean();

	public ThreadPool(int numThreads) {
		threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = newThread();
		}

	}

	private Thread newThread() {
		return new Thread(() -> {
			for (; ; ) {
				try {
					final Runnable task = queue.take();
					if (task == SHUTDOWN_TASK) {
						break;
					}
					task.run();
				} catch (Throwable t) {
					if (!(t instanceof InterruptedException)) {
						System.out.println("Exception: ");
						t.printStackTrace();
					}
				}
			}
			System.err.println("Shutting thread :" + Thread.currentThread().getName());
		});
	}

	@Override
	public void execute(Runnable command) {
		if (started.compareAndSet(false, true)) {
			for (Thread thread : threads) {
				thread.start();
			}
		}
		if (shutdown.get()) {
			throw new RejectedExecutionException();
		}
		queue.add(command);
		if(shutdown.get()){
			queue.remove(command);
			throw new RejectedExecutionException();
		}
	}

	public void shutdown() {
		//shutdown 이 여러번 호출되는 것 방어
		if(shutdown.compareAndSet(false,true)) {
			for (int i = 0; i < threads.length; i++) {
				queue.add(SHUTDOWN_TASK);
			}
		}
		for (Thread thread : threads) {
			do {
				try {
					thread.join();
				} catch (InterruptedException e) {

				}
			} while (thread.isAlive());

		}
	}
}
