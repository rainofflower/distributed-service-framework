package com.yanghui.distributed.framework.bootstrap;

import com.yanghui.distributed.framework.common.RpcConstants;
import com.yanghui.distributed.framework.common.cache.ReflectCache;
import com.yanghui.distributed.framework.common.util.CommonUtils;
import com.yanghui.distributed.framework.common.util.StringUtils;
import com.yanghui.distributed.framework.config.ProviderConfig;
import com.yanghui.distributed.framework.config.ProviderMethodConfig;
import com.yanghui.distributed.framework.config.RegistryConfig;
import com.yanghui.distributed.framework.config.ServerConfig;
import com.yanghui.distributed.framework.handler.CommandHandler;
import com.yanghui.distributed.framework.handler.CommandHandlerPipeline;
import com.yanghui.distributed.framework.protocol.rainofflower.RainofflowerExceptionHandler;
import com.yanghui.distributed.framework.protocol.rainofflower.RainofflowerRpcHandler;
import com.yanghui.distributed.framework.registry.Registry;
import com.yanghui.distributed.framework.registry.RegistryFactory;
import com.yanghui.distributed.framework.server.Server;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 提供者启动器
 *
 * @author YangHui
 */
@Slf4j
public class ProviderBootstrap<T> {

    protected ProviderConfig<T> providerConfig;

    private AtomicBoolean exported = new AtomicBoolean(false);

    public ProviderBootstrap(ProviderConfig<T> providerConfig){
        this.providerConfig = providerConfig;
    }

    /**
     * 发布一个服务
     * 缓存方法，
     * 初始化不同协议的server,
     * 初始化服务方法调用的pipeline，绑定到每个server上
     * 注册方法
     */
    public void export(){
        if(exported.compareAndSet(false, true)){
            List<ServerConfig> serverConfigs = providerConfig.getServer();
            //发布不同协议
            for(ServerConfig serverConfig : serverConfigs){
                Server server = serverConfig.buildIfAbsent();
                server.start();
                Class<T> proxyClass = providerConfig.getProxyClass();
                String interfaceName = providerConfig.getInterfaceName();
                //只发布public方法
                Method[] methods = proxyClass.getMethods();
                String excludeMethodStr = providerConfig.getExclude();
                String[] excludeMethods = StringUtils.splitWithCommaOrSemicolon(excludeMethodStr);
                Map<Method, ProviderMethodConfig> methodConfigs = providerConfig.getMethodConfigs();
                if(methodConfigs == null){
                    methodConfigs = new HashMap<>();
                }
                nextMethod:
                for(Method method : methods){
                    String methodName = method.getName();
                    for(String excludeMethod : excludeMethods){
                        //排除方法
                        if(excludeMethod.equals(methodName)){
                            continue nextMethod;
                        }
                    }
                    //缓存需要注册的方法
                    ReflectCache.putMethodCache(interfaceName,method);
                    //创建当前方法的pipeline
                    CommandHandlerPipeline bizPipeline = new CommandHandlerPipeline();
                    switch(serverConfig.getProtocol()){
                        case RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER:
                            CommandHandler rainofflowerRpcHandler = new RainofflowerRpcHandler(providerConfig.getRef());
                            CommandHandler rainofflowerExceptionHandler = new RainofflowerExceptionHandler();
                            bizPipeline
                                    .addLast(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER+"#defaultHandler",rainofflowerRpcHandler)
                                    .addLast(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER+"#defaultExceptionHandler",rainofflowerExceptionHandler);

                            break;
                        default:
                            rainofflowerRpcHandler = new RainofflowerRpcHandler(providerConfig.getRef());
                            rainofflowerExceptionHandler = new RainofflowerExceptionHandler();
                            bizPipeline
                                    .addLast(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER+"#defaultHandler",rainofflowerRpcHandler)
                                    .addLast(RpcConstants.PROTOCOL_TYPE_RAINOFFLOWER+"#defaultExceptionHandler",rainofflowerExceptionHandler);
                    }
                    bizPipeline.setMethod(method);
                    ThreadPoolExecutor executor;
                    ProviderMethodConfig methodConfig = methodConfigs.get(method);
                    if(methodConfig != null){
                        ThreadPoolExecutor methodExecutor = methodConfig.getExecutor();
                        if(methodExecutor != null){
                            executor = methodExecutor;
                        }else{
                            ThreadPoolExecutor providerExecutor = providerConfig.getExecutor();
                            if(providerExecutor != null){
                                executor = providerExecutor;
                            }else{
                                executor = server.getDefaultBizThreadPool();
                            }
                        }
                    }else{
                        ThreadPoolExecutor providerExecutor = providerConfig.getExecutor();
                        if(providerExecutor != null){
                            executor = providerExecutor;
                        }else{
                            executor = server.getDefaultBizThreadPool();
                        }
                    }
                    //设置执行当前方法的线程池
                    bizPipeline.setExecutor(executor);
                    //注册当前方法业务处理的pipeline
                    server.registryBizPipeline(method, bizPipeline);
                }
            }
            if(providerConfig.isRegister()){
                //需要注册
                List<RegistryConfig> registryConfigs = providerConfig.getRegistry();
                if(CommonUtils.isNotEmpty(registryConfigs)){
                    //遍历多种注册中心
                    for(RegistryConfig registryConfig : registryConfigs){
                        //初始化注册中心
                        //某一个注册中心初始化失败就会抛出异常
                        Registry registry = RegistryFactory.getRegistry(registryConfig);
                        registry.start();
                        registry.register(providerConfig);
                    }
                }
            }
        }
        else{
            log.warn("{}已被其它线程导出",providerConfig.getInterfaceName());
        }
    }

}
