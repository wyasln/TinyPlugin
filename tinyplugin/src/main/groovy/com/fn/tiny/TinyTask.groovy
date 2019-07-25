package com.fn.tiny


import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * TinyTask
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyTask extends DefaultTask {

    def android
    TinyGradleConfig configuration
    String logFilePath

    TinyTask() {
        description = TinyConstant.TASK_DESCRIPTION
        group = TinyConstant.TASK_GROUP
        outputs.upToDateWhen { false }
        android = project.extensions.android
        configuration = project.tinyConfig
        logFilePath = "${project.projectDir}/${TinyConstant.LOG_FILE_NAME}"
    }

    @TaskAction
    def run() {
        configuration.printConfigInfo()
        if (configuration.resourceDir.isEmpty()) {
            println("no resources directory found,compressAllDirectory task cancel!!!")
            return
        }
        if (configuration.apiKey.isEmpty()) {
            println("no api key set,compressAllDirectory task cancel!!!")
            return
        }
        List<TinyItemInfo> compressedList = new ArrayList<>()
        File compressedPicLogFile = new File(logFilePath)
        if (compressedPicLogFile.exists()) {
            try {
                def list = new JsonSlurper().parse(compressedPicLogFile, TinyConstant.UTF8)
                if (list instanceof ArrayList) {
                    compressedList = list
                } else {
                    println("log file is invalid")
                }
            } catch (Exception ignored) {
                println("log file  is invalid")
            }
        } else {
            compressedPicLogFile.createNewFile()
            println("create compressAllDirectory log file")
        }
        def beforeSize = 0L
        def afterSize = 0L
        def newCompressedList = new ArrayList<TinyItemInfo>()
        //压缩程序
        TinyCompress tinyCompress = new TinyCompress(project.projectDir.absolutePath, configuration)

        boolean continueNextDirectory = true
        configuration.resourceDir.each { directory ->
            TinyUtils.printLineSeparator(2)
            println("compressing resources dirctory now >>> ${directory}")
            File resourcesDirectory = new File(directory)
            if (resourcesDirectory.exists() && resourcesDirectory.isDirectory() && continueNextDirectory) {
                TinyResult dirResult = tinyCompress.compressAllDirectory(resourcesDirectory, compressedList)
                continueNextDirectory = dirResult.continueNextDir
                beforeSize += dirResult.rawSize
                afterSize += dirResult.compressedSize
                if (!dirResult.results.isEmpty()) {
                    newCompressedList.addAll(dirResult.results)
                }
            } else {
                if (!resourcesDirectory.exists()) {
                    println("${directory} is not exists!!!Skip this!!!")
                } else if (!resourcesDirectory.isDirectory()) {
                    println("${directory} is not directory!!!Skip this!!!")
                } else if (!continueNextDirectory) {
                    println("No useful api key!!!Skip ${directory}")
                }
            }
        }
        if (newCompressedList) {
            for (TinyItemInfo newTinyPng : newCompressedList) {
                def index = compressedList.path.indexOf(newTinyPng.path)
                if (index >= 0) {
                    compressedList[index] = newTinyPng
                } else {
                    compressedList.add(0, newTinyPng)
                }
            }
            def jsonOutput = new JsonOutput()
            def json = jsonOutput.toJson(compressedList)
            compressedPicLogFile.write(jsonOutput.prettyPrint(json), TinyConstant.UTF8)
            TinyUtils.printLineSeparator(4)
            println("Task finish, compressAllDirectory file count = ${newCompressedList.size()}, before total size = ${TinyUtils.formatFileSize(beforeSize)} after total size = ${TinyUtils.formatFileSize(afterSize)}")
        }

    }

}