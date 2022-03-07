package com.szq.rpc;

import com.szq.rpc.entity.RpcRequest;

/**
 * @author Ashur
 * @description 客户端类通用接口
 */
public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest);
}
