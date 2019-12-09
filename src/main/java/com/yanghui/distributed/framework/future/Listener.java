package com.yanghui.distributed.framework.future;

import java.util.EventListener;

/**
 * @author YangHui
 */
public interface Listener<F extends Future<?>> extends EventListener {

    void operationComplete(F future) throws Exception;

}
