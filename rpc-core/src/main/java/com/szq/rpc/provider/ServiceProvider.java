package com.szq.rpc.provider;

/**
 * @author Ashur
 * @description 保存和提供服务实例对象
 */
public interface ServiceProvider {

    /**
     * @description 将一个服务注册进注册表
     * @param service 待注册的服务实体
     * @param <T> 服务实体类
     * @return [void]
     */
    <T> void addServiceProvider(T service, Class<T> serviceClass);

    /**
     * @description 根据服务名获取服务实体
     * @param serviceName 服务名称
     * @return [java.lang.Object] 服务实体
     */
    Object getServiceProvider(String serviceName);
}
