package com.szq.rpc.exception;

/**
 * @author Ashur
 * @description 序列化异常
 */
public class SerializeException extends RuntimeException{
    public SerializeException(String msg){
        super((msg));
    }
}
