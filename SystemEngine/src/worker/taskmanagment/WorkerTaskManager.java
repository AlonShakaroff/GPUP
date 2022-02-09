package worker.taskmanagment;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerTaskManager {
    private ThreadPoolExecutor threadPool;
    private Integer numberOfAllocatedThreads;

    public WorkerTaskManager(int threadAmount) {
        threadPool = new ThreadPoolExecutor(threadAmount,threadAmount, 1000000, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        this.numberOfAllocatedThreads = threadAmount;
    }

    public Boolean isThreadPoolFull() {
        return threadPool.getActiveCount() == numberOfAllocatedThreads;
    }
}
