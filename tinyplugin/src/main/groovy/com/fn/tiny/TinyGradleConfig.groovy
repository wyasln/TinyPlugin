package com.fn.tiny

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

    TinyGradleConfig() {
        apiKey = []
        ignoreThreshold = 1
        whiteList = []
        resourceDir = []
        logFileName = TinyConstant.LOG_FILE_NAME
    }

    void printConfigInfo() {
        println("TinyGradleConfig.apiKey = ${apiKey}")
        println("TinyGradleConfig.ignoreThreshold = ${ignoreThreshold}")
        println("TinyGradleConfig.whiteList = ${whiteList}")
        println("TinyGradleConfig.resourceDir = ${resourceDir}")
        println("TinyGradleConfig.logFileName = ${logFileName}")
        TinyUtils.printLineSeparator(2)
    }

}
