package com.yanghui.distributed.framework.listener;

import com.yanghui.distributed.framework.client.Cluster;
import com.yanghui.distributed.framework.client.MethodInfo;
import com.yanghui.distributed.framework.client.MethodProviderInfo;

import java.util.List;

/**
 * @author YangHui
 */
public class ClusterMethodProviderListener implements MethodProviderListener {

    private final Cluster cluster;

    public ClusterMethodProviderListener(Cluster cluster){
        this.cluster = cluster;
    }

    @Override
    public void addMethodProviders(List<MethodProviderInfo> methodProviderInfoList) {
        if(cluster != null){
            cluster.addMethodProviders(methodProviderInfoList);
        }
    }

    @Override
    public void removeMethodProvider(MethodProviderInfo methodProviderInfo) {
        if(cluster != null){
            cluster.removeMethodProvider(methodProviderInfo);
        }
    }

    @Override
    public void updateMethodProviders(MethodInfo methodInfo, List<MethodProviderInfo> methodProviderInfoList) {
        if(cluster != null){
            cluster.updateMethodProviders(methodInfo, methodProviderInfoList);
        }
    }
}
