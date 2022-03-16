package com.szq.rpc.transport.socket.server;

import com.szq.rpc.enumertaion.RpcError;
import com.szq.rpc.exception.RpcException;
import com.szq.rpc.hook.ShutdownHook;
import com.szq.rpc.registry.NacosServiceRegistry;
import com.szq.rpc.provider.ServiceProviderImpl;
import com.szq.rpc.registry.ServiceRegistry;
import com.szq.rpc.serializer.CommonSerializer;
import com.szq.rpc.transport.AbstractRpcServer;
import com.szq.rpc.transport.RpcServer;
import com.szq.rpc.provider.ServiceProvider;
import com.szq.rpc.handler.RequestHandler;
import com.szq.rpc.factory.ThreadPoolFactory;
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
public class SocketServer extends AbstractRpcServer {

    private final ExecutorService threadPool;

    private final CommonSerializer serializer;
    private final RequestHandler requestHandler = new RequestHandler();


    public SocketServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }
    public SocketServer(String host, int port, Integer serializerCode){
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        serializer = CommonSerializer.getByCode(serializerCode);
        //创建线程池
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        //自动注册服务
        scanServices();
    }
    /**
     * @description 服务端启动
     * @param
     * @return [void]
     */
    @Override
    public void start(){
        try(ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动……");
            //添加钩子，服务端关闭时时会注销服务
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            //当未接收到连接请求时，accept()会一直阻塞
            while ((socket = serverSocket.accept()) != null){
                logger.info("客户端连接！{}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        }catch (IOException e){
            logger.info("连接时有错误发生" + e);
        }
    }

    }


