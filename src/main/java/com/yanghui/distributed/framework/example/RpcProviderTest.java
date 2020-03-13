package com.yanghui.distributed.framework.example;

import com.yanghui.distributed.framework.common.RpcConstants;
import com.yanghui.distributed.framework.config.ProviderConfig;
import com.yanghui.distributed.framework.config.RegistryConfig;
import com.yanghui.distributed.framework.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * Created by YangHui on 2019/11/22
 */
@Slf4j
public class RpcProviderTest {

    public static void main(String... a) {
        try {
            ServerConfig serverConfig = new ServerConfig()
                    .setProtocol(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER)
                    .setHost("localhost")
                    .setPort(8200)
                    .setAliveTime(6000)
                    .setCoreThreads(5)
                    .setIoThreads(5)
                    .setMaxThreads(10)
                    .setQueues(2000)
                    .setIdleTime(150);
//            RegistryConfig registryConfig = new RegistryConfig()
//                    .setAddress("192.168.43.151:2181");
            ProviderConfig<EchoService> providerConfig = new ProviderConfig<EchoService>()
                    .setServer(serverConfig)
                    .setRef(new EchoServiceImpl())
                    .setInterfaceName(EchoService.class.getName());
//                    .setRegistry(Collections.singletonList(registryConfig));
                    //排除某个方法
//                    .setExclude("friend");
            providerConfig.export();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
