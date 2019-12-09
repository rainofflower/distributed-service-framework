package com.yanghui.distributed.framework.client.router;

import com.yanghui.distributed.framework.config.ConsumerConfig;

/**
 * @author YangHui
 */
public interface Router {

    /**
     * 初始化
     *
     * @param config 服务消费者配置
     */
    void init(ConsumerConfig config);

}
