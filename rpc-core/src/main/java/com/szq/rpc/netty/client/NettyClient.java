package com.szq.rpc.netty.client;

import com.szq.rpc.client.RpcClient;
import com.szq.rpc.codec.CommonDecoder;
import com.szq.rpc.codec.CommonEncoder;
import com.szq.rpc.entity.RpcRequest;
import com.szq.rpc.entity.RpcResponse;
import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import com.szq.rpc.serializer.CommonSerializer;
import com.szq.rpc.serializer.JsonSerializer;
import com.szq.rpc.serializer.KryoSerializer;
import com.szq.rpc.util.RpcMessageChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

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
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);


    private CommonSerializer serializer;

    private String host;
    private int port;
    public NettyClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        //保证自定义实体变量的原子性和共享性的线程安全,此处应用于rpcResponse
        AtomicReference<Object> result = new AtomicReference<>();
        try {
            Channel channel = ChannelProvider.get(new InetSocketAddress(host,port), serializer);
            if (channel.isActive()){
                //向服务端发请求，并设置监听，关于writeAndFlush()的具体实现可以参考：https://blog.csdn.net/qq_34436819/article/details/103937188
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if(future1.isSuccess()){
                        logger.info(String.format("客户端发送消息：%s", rpcRequest.toString()));
                    }else {
                        logger.error("发送消息时有错误发生:", future1.cause());
                    }
                });
                channel.closeFuture().sync();
                //AttributeMap<AttributeKey, AttributeValue>是绑定在Channel上的，可以设置用来获取通道对象
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                //get()阻塞获取value
                RpcResponse rpcResponse = channel.attr(key).get();
                RpcMessageChecker.check(rpcRequest, rpcResponse);
                result.set(rpcResponse.getData());
            }else {
                //0表示”正常“退出程序，即如果当前程序还有在执行的任务，则等待所有任务执行完成以后再退出
                System.exit(0);
            }
        }catch (InterruptedException e){
            logger.error("发送消息时有错误发生:", e);
        }//注意并没有调用shutdown关闭客户端Netty
        return result.get();
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}