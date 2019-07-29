package com.fn.tiny.bean;

/**
 * FaultInfo
 * 压缩过程中的错误条目
 * Author ye.xue
 * Date 2019/7/29
 */
class FaultInfo {

    public String path
    public String errorMessage

    FaultInfo(String path, String errorMessage) {
        this.path = path
        this.errorMessage = errorMessage
    }
}
