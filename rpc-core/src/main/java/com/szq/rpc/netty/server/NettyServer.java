package com.szq.rpc.netty.server;

import com.szq.rpc.serializer.KryoSerializer;
import com.szq.rpc.server.RpcServer;
import com.szq.rpc.codec.CommonDecoder;
import com.szq.rpc.codec.CommonEncoder;
import com.szq.rpc.serializer.JsonSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ashur
 * @desc 动态规划一般用于求最大值问题
 * 链表题可看情况添加伪节点，可以不用单独判断头节点为空的情况
 * 二维数组题可看情况增加一行一列，不用单独处理特殊情况
 * 所有二叉树的题都要先考虑遍历方式
 * 优先队列(堆)的思路是很朴素的。由于找第 K 大元素，其实就是整个数组排序以后后半部分最小的那个元素。因此，我们可以维护一个有 K 个元素的最小堆：
 * new PriorityQueue<>((x,y)->(y-x));//修改默认参数成大根堆
 * 面试的时候经常碰见诸如获取倒数第k个元素，获取中间位置的元素，
 * 判断链表是否存在环，判断环的长度等和长度与位置有关的问题。这些问题都可以通过灵活运用双指针来解决。
 * 对链表操作记得把最后一位的下一位置为null
 * Arrays.sort(intervals, (v1, v2) -> v1[0] - v2[0]); 假设传来两个值，v1 与 v2，那么他们的先后顺序以 v1[0] 比 v2[0] 的结果为准，
 * 即：若 v1[0] < v2[0] 则 v1 < v2，若 = 则 =，若 > 则 >
 */
public class NettyServer implements RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    @Override
    public void start(int port) {
        //用于处理客户端新连接的主“线程池”
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //用于连接后处理IO事件的从“线程池”
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //初始化Netty服务端启动器，作为服务端入口
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //将主从“线程池”初始化到启动器中
            serverBootstrap.group(bossGroup, workerGroup)
            //设置服务端通道类型
            .channel(NioServerSocketChannel.class)
            //日志打印方式
            .handler(new LoggingHandler(LogLevel.INFO))
            //配置ServerChannel参数，服务端接受连接的最大队列长度，如果队列已满，客户端连接将被拒绝。理解可参考：https://blog.csdn.net/fd2025/article/details/79740226
            .option(ChannelOption.SO_BACKLOG,256)
            //启用该功能时，TCP会主动探测空闲连接的有效性。可以将此功能视为TCP的心跳机制，默认的心跳间隔是7200s即2小时。
            .option(ChannelOption.SO_KEEPALIVE, true)
            //配置Channel参数，nodelay没有延迟，true就代表禁用Nagle算法，减小传输延迟。理解可参考：https://blog.csdn.net/lclwjl/article/details/80154565
            .childOption(ChannelOption.TCP_NODELAY, true)
            //初始化Handler,设置Handler操作
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //初始化管道
                    ChannelPipeline pipeline = ch.pipeline();
                    //往管道中添加Handler，注意入站Handler与出站Handler都必须按实际执行顺序添加，比如先解码再Server处理，那Decoder()就要放在前面。
                    //但入站和出站Handler之间则互不影响，这里我就是先添加的出站Handler再添加的入站
                    pipeline.addLast(new CommonEncoder(new KryoSerializer()))
                            .addLast(new CommonDecoder())
                            .addLast(new NettyServerHandler());
                }
            });
            //绑定端口，启动Netty，sync()代表阻塞主Server线程，以执行Netty线程，如果不阻塞Netty就直接被下面shutdown了
            ChannelFuture future = serverBootstrap.bind(port).sync();
            //等确定通道关闭了，关闭future回到主Server
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
            logger.error("启动服务器时有错误发生", e);
        }finally {
            //优雅关闭Netty服务端且清理掉内存，shutdownGracefully()执行逻辑参考：https://www.icode9.com/content-4-797057.html
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
