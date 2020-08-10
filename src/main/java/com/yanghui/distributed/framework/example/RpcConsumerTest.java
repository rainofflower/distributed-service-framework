package com.yanghui.distributed.framework.example;

import com.alibaba.fastjson.JSON;
import com.yanghui.distributed.framework.common.RpcConstants;
import com.yanghui.distributed.framework.config.ConsumerConfig;
import com.yanghui.distributed.framework.config.RegistryConfig;
import com.yanghui.distributed.framework.context.RpcInvokeContext;
import com.yanghui.distributed.framework.core.ResponseFuture;
import com.yanghui.distributed.framework.core.exception.RpcException;
import com.yanghui.distributed.framework.concurrent.Future;
import com.yanghui.distributed.framework.concurrent.Listener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * YangHui on 2019/11/22
 */
@Slf4j
public class RpcConsumerTest {

    public static void main(String... a) {
        try {
            /*RegistryConfig registryConfig = new RegistryConfig()
                    .setAddress("192.168.43.151:2181");*/
            ConsumerConfig<EchoService> consumerConfigSync = new ConsumerConfig<EchoService>()
                    .setInvokeType(RpcConstants.INVOKER_TYPE_SYNC)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(4000)
//                    .setRegistry(Collections.singletonList(registryConfig));
                    .setDirectUrl("localhost:8200");
            EchoService echoServiceSync = consumerConfigSync.refer();

            ConsumerConfig<EchoService> consumerConfigFuture = new ConsumerConfig<EchoService>()
                    .setInvokeType(RpcConstants.INVOKER_TYPE_FUTURE)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(4000)
                    .setDirectUrl("localhost:8200");
            EchoService echoServiceFuture = consumerConfigFuture.refer();

            ConsumerConfig<EchoService> consumerConfigCallback = new ConsumerConfig<EchoService>()
                    .setInvokeType(RpcConstants.INVOKER_TYPE_CALLBACK)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
                    .setTimeout(5000)
//                    .setRegistry(Collections.singletonList(registryConfig))
                    .setDirectUrl("localhost:8200")
                    .setResponseListener(new Listener() {
                        @Override
                        public void operationComplete(Future future) throws Exception {
                            if(future.isSuccess()){
                                Object result = future.get();
                                log.info("callback--:{}",result.toString());
                            }else{
                                Throwable failure = future.getFailure();
                                log.error(failure.toString());
                                failure.printStackTrace();
                                if(failure instanceof RpcException){

                                }else{

                                }
                            }
                        }
                    });
            EchoService echoServiceCallback = consumerConfigCallback.refer();

            ConsumerConfig<EchoService> consumerConfigOneWay = new ConsumerConfig<EchoService>()
                    .setInvokeType(RpcConstants.INVOKER_TYPE_ONEWAY)
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setInterfaceName(EchoService.class.getName())
//                    .setRegistry(Collections.singletonList(registryConfig));
                    .setDirectUrl("localhost:8200");
            EchoService echoServiceOneWay = consumerConfigOneWay.refer();

            int count = 1000;
            ExecutorService pool = Executors.newFixedThreadPool(32);
            for(int i = 0; i<count; i++){
                pool.execute(()->{
                    try {
                        try {
                            String result = echoServiceSync.echo("sync调用");
                            log.info("sync---echo：{}", result);
                        } catch (Exception e) {
                            log.info("sync---调用echo发生错误: ", e);
                        }
                        try {
                            User david = echoServiceSync.getUserByName("david");
                            log.info("sync---getUserByName：{}", JSON.toJSONString(david));
                        } catch (Exception e) {
                            log.info("sync---调用getUserByName发生异常：", e);
                        }
                        try {
                            List<User> list = echoServiceSync.listUser("eden");
                            log.info("sync---listUser：{}", JSON.toJSONString(list));
                        } catch (Exception e) {
                            log.info("sync---调用listUser发生异常：", e);
                        }
                        User user = new User();
                        user.setName("test");
                        user.setAge(23);
                        try {
                            String friend = echoServiceSync.friend(user, 20, 25, "rainofflower");
                            log.info("sync---friend:" + friend);
                        } catch (Exception e) {
                            log.info("sync---friend发生错误: ", e);
                        }

                        echoServiceFuture.echo("future调用");
                        String sFuture = (String) ResponseFuture.getResponse(4000, TimeUnit.MILLISECONDS, true);
                        log.info("future---echo：{}", sFuture);

                        echoServiceCallback.echo("callback调用");

                        List<User> users = new ArrayList<>();
                        users.add(new User("spring", 1));
                        users.add(new User("summer", 2));
                        users.add(new User("spring", 3));
                        users.add(new User("autumn", 3));
                        users.add(new User("winter", 3));
                        users.add(new User("spring", 3));
                        users.add(new User("winter", 3));
                        echoServiceOneWay.oneWayTest(users, "oneWay调用");

                        log.info("sync---test2:" + echoServiceSync.test2());

                        echoServiceOneWay.test2();

                        echoServiceCallback.test2();

                        List<User> winter = echoServiceSync.matchUser(users, "winter");
                        log.info("sync---matchUser:{}", JSON.toJSONString(winter));

                        echoServiceFuture.matchUser(users, "spring");
                        List<User> future2 = (List<User>) ResponseFuture.getResponse(4000, TimeUnit.MILLISECONDS, true);
                        log.info("future---matchUser：{}", JSON.toJSONString(future2));

                        echoServiceCallback.matchUser(users, "summer");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
