package com.yanghui.distributed.framework.client;

import com.yanghui.distributed.framework.bootstrap.ConsumerBootstrap;
import com.yanghui.distributed.framework.core.Request;
import com.yanghui.distributed.framework.core.Response;
import com.yanghui.distributed.framework.core.exception.RpcException;

/**
 * 集群容错->失败自动切换
 * @author YangHui
 */
public class FailoverCluster extends Cluster {

    public FailoverCluster(ConsumerBootstrap bootstrap){
        super(bootstrap);
    }

    /**
     * 需实现失败自动切换逻辑
     */
    @Override
    public Response doInvoke(Request request) throws RpcException {
        MethodProviderInfo methodProviderInfo = select(request);
        return sendMsg(methodProviderInfo, request);
    }
}
