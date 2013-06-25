package study;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * newTaskForでタスクの標準的でないキャンセルをカプセル化する
 *
 * List 7-12
 */
public class NewTaskForSample {
    public interface CancellableTask<T> extends Callable<T> {
        void cancel();
        RunnableFuture<T> newTask();
    }

    public class CancellingExecutor extends ThreadPoolExecutor {
        public CancellingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
            if(callable instanceof CancellableTask) {
                return ((CancellableTask<T>)callable).newTask();
            } else {
                return super.newTaskFor(callable);
            }
        }
    }

    public abstract class SocketUsingTask<T> implements CancellableTask<T> {
        private Socket socket;

        protected synchronized void setSocket(Socket s) {
            this.socket = s;
        }

        @Override
        public synchronized void cancel() {
            try {
                if(socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                // 無視
                e.printStackTrace();
            }
        }

        @Override
        public RunnableFuture<T> newTask() {
            return new FutureTask<T>(this) {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    try {
                        SocketUsingTask.this.cancel();
                    } finally {
                        return super.cancel(mayInterruptIfRunning);
                    }
                }
            };
        }
    }
}
