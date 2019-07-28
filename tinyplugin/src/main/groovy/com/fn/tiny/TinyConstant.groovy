package com.fn.tiny;

/**
 * TinyConstant
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyConstant {

    //任务名称
    public static final String TASK_NAME = "tinyPng"
    //任务组
    public static final String TASK_GROUP = "tinypng"
    //任务描述
    public static final String TASK_DESCRIPTION = "TinyPng compressAllDirectory project picture files"
    //任务配置
    public static final String GRADLE_CONFIG_NAME = "tinyConfig"
    //压缩记录文件名称
    public static final String LOG_FILE_NAME = "tiny_compressed_record.json"

    //文件编码
    public static final String UTF8 = "UTF-8"

    public static final int TASK_NORMAL = 0
    public static final int TASK_CHANGE_KEY = 1
    public static final int TASK_KEY_FAULT = -1
    public static final int TASK_SERVER_FAULT = -2
    public static final int TASK_CONNECTION_FAULT = -3
    public static final int TASK_IO_FAULT = -4
    public static final int TASK_UNKNOWN_FAULT = -5

}
