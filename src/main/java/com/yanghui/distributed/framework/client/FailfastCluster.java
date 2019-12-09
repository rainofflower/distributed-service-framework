package com.yanghui.distributed.framework.client;

import com.yanghui.distributed.framework.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.framework.core.Request;
import com.yanghui.distributed.framework.core.Response;
import com.yanghui.distributed.framework.core.exception.RpcException;

/**
 * 集群容错->快速失败
 * @author YangHui
 */
public class FailfastCluster extends Cluster{

    public FailfastCluster(ConsumerBootstrap bootstrap){
        super(bootstrap);
    }

    @Override
    public Response doInvoke(Request request) throws RpcException {
        MethodProviderInfo methodProviderInfo = select(request);
        return sendMsg(methodProviderInfo, request);
    }
}
