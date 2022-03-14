package com.szq.rpc.registry;

import java.net.InetSocketAddress;

/**
 * @author Ashur
 * @description 服务注册中心通用接口
 */
public interface ServiceRegistry {
    /**
     * @description 将一个服务注册到注册表
     * @param serviceName, inetSocketAddress 服务名称，提供服务的地址
     * @return [void]
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);


}
