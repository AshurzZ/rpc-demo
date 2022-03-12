package com.szq.rpc.enumertaion;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Ashur
 * @description 字节流中标识序列化和反序列化器
 */
@AllArgsConstructor
@Getter
public enum SerializerCode {
    KRYO(0),
    JSON(1),
    HESSIAN(2),
    PROTOBUFF(3);
    private final int code;
}
