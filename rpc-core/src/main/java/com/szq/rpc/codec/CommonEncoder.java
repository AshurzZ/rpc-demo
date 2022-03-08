package com.szq.rpc.codec;

import com.szq.rpc.entity.RpcRequest;
import com.szq.rpc.enumertaion.PackageType;
import com.szq.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static java.lang.System.out;


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

public class CommonEncoder extends MessageToByteEncoder {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer serializer;

    public CommonEncoder(CommonSerializer serializer){
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        //数据写到缓冲区
        out.writeInt(MAGIC_NUMBER);
        if(msg instanceof RpcRequest){
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        }else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }
        out.writeInt(serializer.getCode());
        byte[] bytes = serializer.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}