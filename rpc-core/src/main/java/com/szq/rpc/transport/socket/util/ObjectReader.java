package com.szq.rpc.transport.socket.util;

import com.szq.rpc.entity.RpcRequest;
import com.szq.rpc.entity.RpcResponse;
import com.szq.rpc.enumertaion.PackageType;
import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import com.szq.rpc.serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ashur
 * @description Socket方式从输入流中读取字节并反序列化【解码】
 */
public class ObjectReader {
    private static final Logger logger = LoggerFactory.getLogger(ObjectReader.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static Object readObject(InputStream in) throws IOException{
        byte[] numbersBytes = new byte[4];
        in.read(numbersBytes);
        int magic = bytesToInt(numbersBytes);
        if(magic != MAGIC_NUMBER) {
            logger.error("不识别的协议包：{}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }
        in.read(numbersBytes);
        int packageCode = bytesToInt(numbersBytes);
        Class<?> packageClass;
        if(packageCode == PackageType.REQUEST_PACK.getCode()){
            packageClass = RpcRequest.class;
        }else if (packageCode == PackageType.RESPONSE_PACK.getCode()){
            packageClass = RpcResponse.class;
        }else {
            logger.error("不识别的数据包：{}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }
        in.read(numbersBytes);
        int serializerCode = bytesToInt(numbersBytes);
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            logger.error("不识别的反序列化器：{}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }
        in.read(numbersBytes);
        int length = bytesToInt(numbersBytes);
        byte[] bytes = new byte[length];
        in.read(bytes);
        return serializer.deserialize(bytes, packageClass);
    }
    /**
     * @description 字节数组转换为Int
     * @param src
     * @return [int]
     */
    private static int bytesToInt(byte[] src) {
        int value;
        value = ((src[0] & 0xFF) << 24)
                |((src[1] & 0xFF) << 16)
                |((src[2] & 0xFF) << 8)
                |(src[3] & 0xFF);
        return value;
    }
}
