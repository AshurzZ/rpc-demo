package com.szq.rpc.client;

import com.szq.rpc.entity.RpcRequest;
import com.szq.rpc.serializer.CommonSerializer;

/**
 * @author Ashur
 * @description 客户端类通用接口
 */
public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest);
    void setSerializer(CommonSerializer serializer);
}
