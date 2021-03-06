package com.szq.rpc.transport.netty.client;

import com.szq.rpc.codec.CommonDecoder;
import com.szq.rpc.codec.CommonEncoder;
import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import com.szq.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Ashur
 * @description 用于获取Channel对象
 */
public class ChannelProvider {
    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);
    private static EventLoopGroup eventLoopGroup;
    private static Bootstrap bootstrap = initializeBootstrap();

    /**
     * 所有客户端Channel都保存在该Map中
     */
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    private static Bootstrap initializeBootstrap() {
        eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                //连接的超时时间，超过这个时间还是建立不上则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                //启动该功能时，TCP会主动探测空闲连接的有效性。可以将此功能视为TCP的心跳机制，默认的心跳间隔是7200s即两小时。
                .option(ChannelOption.SO_KEEPALIVE, true)
                //配置Channle参数，nodelay没有延迟，true就代表禁用Nagle算法，减小传输延迟
                //理解可参考：https://blog.csdn.net/lclwjl/article/details/80154565
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap;
    }

    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer){
        String key = inetSocketAddress.toString() + serializer.getCode();
        if (channels.containsKey(key)){
            Channel channel = channels.get(key);
            if (channel != null && channel.isActive()){
                return channel;
            } else {
                channels.remove(key);
            }
        }


        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new CommonEncoder(serializer))
                        //设定IdleStateHandler心跳检测每5秒进行一次写检测，如果5秒内write()方法未被调用则触发一次userEventTrigger()方法
                        //实现客户端每5秒向服务端发送一次消息
                        .addLast(new IdleStateHandler(0,5, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());
            }
        });
        Channel channel = null;
        try{
          channel = connect(bootstrap, inetSocketAddress);
        }catch (ExecutionException | InterruptedException e){
            logger.error("获取Channel时有错误发生", e);
            return null;
        }
        channels.put(key, channel);
        return channel;
    }
    /**
     * @description Netty客户端创建通道连接
     * @param bootstrap, inetSocketAddress
     * @return [void]
     */
    private static Channel connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) futuer ->{
            if (futuer.isSuccess()){
                logger.info("客户端连接成功");
                completableFuture.complete(futuer.channel());
            }else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }




}
