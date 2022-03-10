package com.szq.rpc.socket.server;

import com.szq.rpc.entity.RpcRequest;
import com.szq.rpc.entity.RpcResponse;
import com.szq.rpc.registry.ServiceRegistry;
import com.szq.rpc.serializer.CommonSerializer;
import com.szq.rpc.server.RequestHandler;
import com.szq.rpc.util.ObjectReader;
import com.szq.rpc.util.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * @author Ashur
 * description IO传输模式 处理客户端RpcRequest的工作线程
 */
public class RequestHandlerThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    private Socket socket;
    private RequestHandler requestHandler;
    private ServiceRegistry serviceRegistry;
    private CommonSerializer serializer;
    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceRegistry serviceRegistry,CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
    }

    @Override
    public void run() {
        try(InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream()) {
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceRegistry.getService(interfaceName);
            Object result = requestHandler.handle(rpcRequest, service);
            RpcResponse<Object> response = RpcResponse.success(result);
            ObjectWriter.writeObject(outputStream, response, serializer);
        }catch (IOException e){
            logger.info("调用或发送时发生错误：" + e);
        }
    }
}
