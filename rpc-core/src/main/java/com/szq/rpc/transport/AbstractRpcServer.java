package com.szq.rpc.transport;

import com.szq.rpc.annotation.Service;
import com.szq.rpc.annotation.ServiceScan;
import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import com.szq.rpc.provider.ServiceProvider;
import com.szq.rpc.registry.ServiceRegistry;
import com.szq.rpc.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @author Ashur
 * @description 扫描服务类并进行服务注册(模板模式）
 *由于扫描和注册服务是一个比较公共的方法 ， 无论是Socket还是Netty的服务端都需要这个方法 ，
 * 因此考虑采用 “ 模板模式 ” 对相关操作进行重构 ， 即顶层是RpcServer接口 ，
 * 中间层使用一个抽象类AbstractRpcServer实现RpcServer接口中的公共方法scanServices ()和publishService()，
 * 底层的NettyServer和SocketServer则继承AbstractRpcServer抽象类实现start()方法，在方法中写各自独有的逻辑。
 */
public abstract class AbstractRpcServer implements RpcServer{

    protected Logger logger = LoggerFactory.getLogger(AbstractRpcServer.class);

    protected String host;
    protected int port;

    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;

    public void scanServices(){
        //获取main()入口所在类的类名，即启动类
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            //获取启动类对应的实例对象
            startClass = Class.forName(mainClassName);
            //判断启动类是否存在ServiceScan注解
            if(!startClass.isAnnotationPresent(ServiceScan.class)){
                logger.error("启动类缺少@ServiceScan注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        }catch (ClassNotFoundException e){
            logger.info("出现未知错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        //获取ServiceScan注解接口对应value()的值，默认设置的“”
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if("".equals(basePackage)){
            //获取启动类所在的包，因为服务类也放在这个包下面的
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        //获取包下面的所有类Class对象
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for(Class<?> clazz : classSet){
            //利用Service注解判断该类是否为服务类
            if(clazz.isAnnotationPresent(Service.class)){
                //获取Service注解接口对应name()的值，默认设置的“”
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try{
                    //创建服务Impl类的实例
                    obj = clazz.newInstance();
                }catch (IllegalAccessException | InstantiationException e){
                    logger.error("创建" + clazz + "时有错误发生");
                    continue;
                }
                if("".equals(serviceName)){
                    //一个服务Impl类可能实现了多个服务接口
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for(Class<?> oneInterface : interfaces){
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                }else {
                    publishService(obj, serviceName);
                }
            }
        }
    }

    /**
     * @description 将服务保存在本地的注册表，同时注册到Nacos
     * @param service, serviceName
     * @return [void]
     */
    @Override
    public <T> void publishService(T service, String serviceName){
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }

}