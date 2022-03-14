package com.szq.rpc.transport.socket.server;

import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import com.szq.rpc.registry.NacosServiceRegistry;
import com.szq.rpc.registry.ServiceProviderImpl;
import com.szq.rpc.registry.ServiceRegistry;
import com.szq.rpc.serializer.CommonSerializer;
import com.szq.rpc.transport.RpcServer;
import com.szq.rpc.provider.ServiceProvider;
import com.szq.rpc.handler.RequestHandler;
import com.szq.rpc.transport.socket.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @description Socket方式进行远程调用连接的服务端
 */
public class SocketServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private final ExecutorService threadPool;
    private final String host;
    private final int port;
    private CommonSerializer serializer;
    private RequestHandler requestHandler = new RequestHandler();
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    public SocketServer(String host, int port) {
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        //创建线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
    }
    /**

     * @description 将服务保存在本地的注册表，同时注册到Nacos注册中心
     * @param service, serviceClass]
     * @return [void]
     */
    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        if (serializer == null){
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        serviceProvider.addServiceProvider(service);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }
    /**
     * @description 服务端启动
     * @param port
     * @return [void]
     */
    @Override
    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器启动……");
            Socket socket;
            //当未接收到连接请求时，accept()会一直阻塞
            while ((socket = serverSocket.accept()) != null){
                logger.info("客户端连接！{}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        }catch (IOException e){
            logger.info("连接时有错误发生" + e);
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }

}
