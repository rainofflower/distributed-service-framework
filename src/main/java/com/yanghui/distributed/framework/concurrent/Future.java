package com.yanghui.distributed.framework.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author YangHui
 */
public interface Future<V> extends java.util.concurrent.Future<V> {

    Future<V> setSuccess(V result);

    void setFailure(Throwable failure);

    Throwable getFailure();

    Future<V> addListener(Listener<? extends Future<V>> listener);

    Future<V> await();

    boolean await(long timeout, TimeUnit unit) throws TimeoutException;

    Future<V> sync();

    boolean isSuccess();
}
