package com.szq.rpc.provider;

import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author Ashur
 * @description 默认的服务注册表，保存服务端本地服务
 */
public class ServiceProviderImpl implements ServiceProvider {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(ServiceProviderImpl.class);
    /**
     * key = 服务名称(即接口名), value = 服务实体(即实现类的实例对象)
     */
    private  static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    /**
     * 用来存放实现类的名称，Set存取更高效，存放实现类名称相比存放接口名称占的空间更小，因为一个实现类可能实现了多个接口
     * * 用来存放服务名称(即接口名）
     */
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();
    /**
     * @description 保存服务到本地服务注册表
     * @param service, serviceClass 服务的实现类对象，服务类（接口）
     * @return [void]
     */
    @Override
    public <T> void addServiceProvider(T service, String serviceName) {
        if (registeredService.contains(serviceName)){
            return;
        }
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);
        logger.info("向接口：{} 注册服务：{}", service.getClass().getInterfaces(),serviceName);
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if(service == null){
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
//在注册服务时，默认采用接口名称作为服务名，例如某个对象 A 实现了接口 X 和 Y，那么将 A 注册进去后，会有两个服务名 X 和 Y 对应于 A 对象。
// 相当于创建了两个map（k,v）对象，这种处理方式的好处在于每个接口只会对应一个对象，逻辑更清晰，查找更方便。同时注意使用了ConcurrentHashMap和Synchronized来保证线程安全。

}
