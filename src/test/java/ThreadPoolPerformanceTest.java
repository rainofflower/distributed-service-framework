import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author YangHui
 */
public class ThreadPoolPerformanceTest {

    private int count = 1000000;

    private AtomicInteger realExecuteCount = new AtomicInteger(0);

    private RateLimiter limiter = RateLimiter.create(count/10);

    @Test
    public void test(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
                5,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE));
        long start1 = System.currentTimeMillis();
        for(int i = 0; i<count; i++){
            limiter.acquire();
            threadPoolExecutor.execute(()->{
                doWork();
            });
        }
        threadPoolExecutor.shutdown();
        boolean terminated = false;
        while(!terminated){
            try {
                terminated = threadPoolExecutor.awaitTermination(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end1 = System.currentTimeMillis();
        System.out.println("ThreadPoolExecutor -- cost "+(end1 - start1) +" ms");
        System.out.println("ThreadPoolExecutor -- 实际执行任务数："+realExecuteCount.get());

        realExecuteCount.set(0);

        NioEventLoopGroup loopGroup = new NioEventLoopGroup(5);
        long start2 = System.currentTimeMillis();
        for(int i = 0; i<count; i++) {
            limiter.acquire();
            loopGroup.execute(() -> {
                doWork();
            });
        }
        loopGroup.shutdownGracefully().syncUninterruptibly();
        long end2 = System.currentTimeMillis();
        System.out.println("EventLoopGroup -- cost "+(end2 - start2) +" ms");
        System.out.println("EventLoopGroup -- 实际执行任务数："+realExecuteCount.get());
    }

    @Test
    public void testRateLimit(){
        long start = System.currentTimeMillis();
        for(int i = 0; i<count; i++) {
            limiter.acquire();
        }
        long end = System.currentTimeMillis();
        System.out.println("cost "+(end - start) +" ms");
    }


    private void doWork(){
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        realExecuteCount.incrementAndGet();
    }
}
