package com.yanghui.distributed.framework.protocol.rainofflower;

import com.yanghui.distributed.framework.handler.CommandHandlerAdapter;
import com.yanghui.distributed.framework.handler.CommandHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Rainofflower协议异常处理器
 *
 * @author YangHui
 */
@Slf4j
public class RainofflowerExceptionHandler extends CommandHandlerAdapter {

    public void handleException(CommandHandlerContext ctx, Throwable throwable){
        log.info("捕获异常，信息：",throwable);
        ctx.fireHandleException(throwable);
    }
}
