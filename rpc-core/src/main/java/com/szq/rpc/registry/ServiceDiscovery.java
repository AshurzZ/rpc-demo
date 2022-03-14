package com.szq.rpc.registry;

import java.net.InetSocketAddress;

/**
 * @author Ashur
 * @description 服务发现接口
 */
public interface ServiceDiscovery {
    /**
     * @description 根据服务名称查找服务端地址
     * @param serviceName
     * @return [java.net.InetSocketAddress]
     */
    InetSocketAddress lookupService(String serviceName);
}
