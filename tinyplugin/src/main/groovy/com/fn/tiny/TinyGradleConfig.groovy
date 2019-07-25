package com.fn.tiny

/**
 * TinyGradleConfig
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyGradleConfig {

    ArrayList<String> apiKey
    long ignoreThreshold
    ArrayList<String> whiteList
    ArrayList<String> resourceDir

    TinyGradleConfig() {
        apiKey = []
        ignoreThreshold = 1
        whiteList = []
        resourceDir = []
    }

    void printConfigInfo() {
        println("TinyGradleConfig.apiKey = ${apiKey}")
        println("TinyGradleConfig.ignoreThreshold = ${ignoreThreshold}")
        println("TinyGradleConfig.whiteList = ${whiteList}")
        println("TinyGradleConfig.resourceDir = ${resourceDir}")
        TinyUtils.printLineSeparator(2)
    }

}
