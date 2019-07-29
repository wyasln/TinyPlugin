package com.fn.tiny.plugin

import com.fn.tiny.TinyConstant
import com.fn.tiny.TinyUtils

/**
 * TinyGradleConfig
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyGradleConfig {

    public ArrayList<String> apiKey
    public long ignoreThreshold
    public ArrayList<String> whiteList
    public ArrayList<String> resourceDir
    public String logFileName
    public long timeout

    TinyGradleConfig() {
        apiKey = []
        ignoreThreshold = 1
        whiteList = []
        resourceDir = []
        logFileName = TinyConstant.LOG_FILE_NAME
        timeout = 60
    }

    void printConfigInfo() {
        println("TinyGradleConfig.apiKey = ${apiKey}")
        println("TinyGradleConfig.ignoreThreshold = ${ignoreThreshold}")
        println("TinyGradleConfig.whiteList = ${whiteList}")
        println("TinyGradleConfig.resourceDir = ${resourceDir}")
        println("TinyGradleConfig.logFileName = ${logFileName}")
        println("TinyGradleConfig.timeout = ${timeout}")
        TinyUtils.printLineSeparator()
    }

}
