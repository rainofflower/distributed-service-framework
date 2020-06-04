import com.yanghui.distributed.framework.concurrent.ClientCallbackExecutor;
import com.yanghui.distributed.framework.concurrent.DefaultPromise;
import com.yanghui.distributed.framework.concurrent.Future;
import com.yanghui.distributed.framework.concurrent.Listener;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author YangHui
 */
@Slf4j
public class PromiseTest {

    @Test
    public void test() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorService pool2 = ClientCallbackExecutor.getInstance().getExecutor();
        DefaultPromise<String> promise = new DefaultPromise<>(new Callable() {
            @Override
            public String call() throws Exception {
                log.info("执行任务");
//                throw new RuntimeException("发生错误");
                return "哈哈";
            }
        }, pool2);
        pool.execute(promise);
        promise.addListener(new Listener<Future<String>>() {
            @Override
            public void operationComplete(Future<String> future) throws Exception {
                log.info("执行回调1");
                if(future.isSuccess()){
                    String s1 = future.get();
                    log.info(s1);
                }else{
                    log.error(future.getFailure().toString());
                }
            }
        });
        String s = null;
        try {
            s = promise.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        log.info(s);
        promise.addListener(new Listener<Future<String>>() {
            @Override
            public void operationComplete(Future<String> future) throws Exception {
                log.info("执行回调2");
                if(future.isSuccess()){
                    String s1 = future.get();
                    log.info(s1);
                }else{
                    log.error(future.getFailure().toString());
                }
            }
        });
        pool.shutdown();
        pool2.shutdown();
        while(!pool.isTerminated() || !pool2.isTerminated()){
            Thread.yield();
        }
    }

    @Test
    public void test2() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorService pool2 = ClientCallbackExecutor.getInstance().getExecutor();
        DefaultPromise<String> promise = new DefaultPromise<>(new Callable() {
            @Override
            public String call() throws Exception {
                log.info("执行任务");
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
//                throw new RuntimeException("发生错误");
                log.info("执行成功");
                return "哈哈";
            }
        }, pool2);
        promise.addListener(new Listener<Future<String>>() {
            @Override
            public void operationComplete(Future<String> future) throws Exception {
                log.info("执行回调1");
                if(future.isSuccess()){
                    String s1 = future.get();
                    log.info(s1);
                }else{
                    log.error(future.getFailure().toString());
                }
            }
        });
        pool.execute(promise);
        String s = null;
        try {
//            promise.sync();

            s = promise.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(s);
        promise.addListener(new Listener<Future<String>>() {
            @Override
            public void operationComplete(Future<String> future) throws Exception {
                log.info("执行回调2");
                if(future.isSuccess()){
                    String s1 = future.get();
                    log.info(s1);
                }else{
                    log.error(future.getFailure().toString());
                }
            }
        });
//        pool.shutdown();
//        pool2.shutdown();
//        while(!pool.isTerminated() || !pool2.isTerminated()){
//            Thread.yield();
//        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test4(){
        ThreadPoolExecutor pool = new ThreadPoolExecutor(5,
                10,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100));
        AtomicInteger atomicInteger = new AtomicInteger();
        int total = 380;
        int batchCount = 110;
        int perCost = 100;
        int batch = total % batchCount == 0 ? total / batchCount : total / batchCount + 1;
        System.out.println("batch num: "+batch);
        for(int b=0; b<batch; b++){
            long start2 = System.currentTimeMillis();
            int min = Math.min((b + 1) * batchCount, total);
            CountDownLatch latch = new CountDownLatch(min-(batchCount*b));
            for(int i = batchCount*b; i<min; i++){
                try{
                    pool.execute(()->{
                        try{
                            work(perCost);
                            atomicInteger.incrementAndGet();
                        }finally {
                            latch.countDown();
                        }
                    });
                }catch (Throwable throwable){
                    throwable.printStackTrace();
                    latch.countDown();
                }
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("run "+atomicInteger.get() + " work\ncost "+(System.currentTimeMillis() - start2)+" ms\n" + "serial cost "+total*perCost);
            System.out.println("----------");
        }
        System.out.println("==========");
        System.out.println("total run "+atomicInteger.get());
    }


    public void work(int mills){
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(mills));
    }
}
