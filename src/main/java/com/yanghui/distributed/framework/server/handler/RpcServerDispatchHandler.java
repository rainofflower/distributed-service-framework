package com.yanghui.distributed.framework.server.handler;

import com.google.protobuf.ProtocolStringList;
import com.yanghui.distributed.framework.common.cache.ReflectCache;
import com.yanghui.distributed.framework.core.exception.ErrorType;
import com.yanghui.distributed.framework.core.exception.RpcException;
import com.yanghui.distributed.framework.handler.CommandHandlerPipeline;
import com.yanghui.distributed.framework.protocol.rainofflower.Rainofflower;
import com.yanghui.distributed.framework.server.Server;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 将请求转发到对应的pipeline中处理
 *
 * @author YangHui
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcServerDispatchHandler extends SimpleChannelInboundHandler {

    private final Server server;

    public RpcServerDispatchHandler(Server server){
        this.server = server;
    }

    public void channelRead0(ChannelHandlerContext ctx, Object msg){
//        String protocol = ctx.channel().attr(Server.PROTOCOL).get();
        Rainofflower.Message message = (Rainofflower.Message) msg;
        Rainofflower.BizRequest bizRequest = message.getBizRequest();
        String interfaceName = bizRequest.getInterfaceName();
        String methodName = bizRequest.getMethodName();
        ProtocolStringList paramTypesList = bizRequest.getParamTypesList();
        int paramCount = paramTypesList.size();
        String[] paramTypes = new String[paramCount];
        if(paramCount != 0){
            for(int i = 0; i<paramCount; i++){
                paramTypes[i] = paramTypesList.get(i);
            }
        }
        Method method = ReflectCache.getMethodCache(interfaceName, methodName, paramTypes);
        //找到当前方法的pipeline并触发链式调用
        CommandHandlerPipeline bizPipeline = server.getBizPipeline(method);
        if(bizPipeline == null){
            throw new RpcException(ErrorType.SERVER_NOT_FOUND_PROVIDER,"未找到服务提供者！");
        }
        bizPipeline.setChannelHandlerContext(ctx)
                .fireHandleCommand(message);
    }
}
