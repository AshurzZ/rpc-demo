package com.szq.rpc.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.szq.rpc.enumertaion.SerializerCode;
import com.szq.rpc.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
public class HessianSerializer implements CommonSerializer{
    private static final Logger logger = LoggerFactory.getLogger(HessianSerializer.class);

    @Override
    public byte[] serialize(Object obj) {
        HessianOutput hessianOutput = null;
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        }catch (IOException e){
            logger.error("序列化时有错误发生" + e);
            throw new SerializeException("序列化时有错误发生");
        }finally {
            if(hessianOutput != null){
                try {
                    hessianOutput.close();
                }catch (IOException e){
                    logger.error("关闭output流时有错误发生" + e);
                }
            }
        }
    }
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        HessianInput hessianInput = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)){
            hessianInput = new HessianInput(byteArrayInputStream);
            return hessianInput.readObject();
        }catch (IOException e){
            logger.error("反序列化时有错误发生" + e);
            throw new SerializeException("反序列化时有错误发生");
        }finally {
            if(hessianInput != null){
                hessianInput.close();
            }
        }
    }
    @Override
    public int getCode() {
        return SerializerCode.valueOf("HESSIAN").getCode();
    }
}
