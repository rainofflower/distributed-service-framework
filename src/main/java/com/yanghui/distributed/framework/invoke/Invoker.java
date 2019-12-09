package com.yanghui.distributed.framework.invoke;

import com.yanghui.distributed.framework.core.Request;
import com.yanghui.distributed.framework.core.Response;
import com.yanghui.distributed.framework.core.exception.RpcException;

/**
 * 调用器
 *
 * @author YangHui
 */
public interface Invoker {

    Response invoke(Request request) throws RpcException;
}
