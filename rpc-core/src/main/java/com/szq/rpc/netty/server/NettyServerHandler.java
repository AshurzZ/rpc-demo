package com.szq.rpc.netty.server;

import com.szq.rpc.entity.RpcRequest;
import com.szq.rpc.entity.RpcResponse;
import com.szq.rpc.registry.DefaultServiceRegistry;
import com.szq.rpc.registry.ServiceRegistry;
import com.szq.rpc.server.RequestHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ashur
 * @description Netty中处理从客户端传来的RpcRequest
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RequestHandler requestHandler;
    private static ServiceRegistry serviceRegistry;

    static {
        requestHandler = new RequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        try {
            logger.info("服务端接收到请求:{}", msg);
            String interfaceName = msg.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = requestHandler.handle(msg, service);
            ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result));
//             ChannelFuture future = ctx.writeAndFlush(result);
            //添加一个监听器到channelfuture来检测是否所有的数据包都发出，然后关闭通道
            future.addListener(ChannelFutureListener.CLOSE);
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理过程调用时有错误发生：");
        cause.printStackTrace();
        ctx.close();
    }
}
