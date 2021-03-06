package com.szq.rpc.handler;

import com.szq.rpc.entity.RpcRequest;
import com.szq.rpc.entity.RpcResponse;
import com.szq.rpc.enumertaion.ResponseCode;
import com.szq.rpc.provider.ServiceProvider;
import com.szq.rpc.provider.ServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author Ashur
 * @description 实际执行方法调用任务的处理器
 */
public class RequestHandler{

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final ServiceProvider serviceProvider;
    static {
        serviceProvider = new ServiceProviderImpl();
    }
    public Object handle(RpcRequest rpcRequest){
        //从服务端本地注册表中获取服务实体
        Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest,Object service){
        Object result;
        try {
            //getClass()获取的是实例对象的类型
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            logger.info("服务: {}成功调用方法: {}",rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
        }
        //方法调用成功
        return RpcResponse.success(result, rpcRequest.getRequestId());
    }
}