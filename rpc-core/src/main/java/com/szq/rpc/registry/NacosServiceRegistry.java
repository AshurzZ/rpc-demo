package com.szq.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import com.szq.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Ashur
 * @description Nacos服务注册中心
 */
public class NacosServiceRegistry implements ServiceRegistry{
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    /**
     * @description 将服务的名称和地址注册进服务注册中心
     * @param serviceName, inetSocketAddress]
     * @return [void]
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            //向Nacos注册服务
            NacosUtil.registerService(serviceName, inetSocketAddress);
        } catch (NacosException e) {
            logger.error("注册服务时有错误发生" + e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }
}
