package com.szq.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Ashur
 * @description 管理Nacos连接等工具类
 */

public class NacosUtil {
    private static final Logger logger = LoggerFactory.getLogger(NacosUtil.class);

    private static final String SERVER_ADDR = "127.0.0.1:8848";
    /**
     * @description 连接到Nacos创建命名空间
     * @param
     * @return [com.alibaba.nacos.api.naming.NamingService]
     */
    public static NamingService getNacosNamingService(){
        try{
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接到Nacos时有错误发生：", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }
    /**
     * @description 注册服务到Nacos
     * @param namingService, serviceName, inetSocketAddress
     * @return [void]
     */
    public static void registerService(NamingService namingService, String serviceName, InetSocketAddress inetSocketAddress) throws NacosException {
        namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
    }
    /**
     * @description 获取所有提供该服务的服务端地址
     * @param namingService, serviceName
     * @return [java.util.List<com.alibaba.nacos.api.naming.pojo.Instance>]
     */
    public  static List<Instance> getAllInstance(NamingService namingService, String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }
}
