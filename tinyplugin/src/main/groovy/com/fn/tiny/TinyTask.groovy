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
    TinyGradleConfig mGradleConfig
    String mCompressedLogFile
    long mIgnoreFileSize
    long mTimeout

    TinyTask() {
        description = TinyConstant.TASK_DESCRIPTION
        group = TinyConstant.TASK_GROUP
        outputs.upToDateWhen { false }
        android = project.extensions.android
        mGradleConfig = project.tinyConfig
        mCompressedLogFile = "${project.projectDir}/${mGradleConfig.logFileName}"
        mIgnoreFileSize = mGradleConfig.ignoreThreshold * 1024
        mTimeout = mGradleConfig.timeout
    }

    @TaskAction
    def run() {
        mGradleConfig.printConfigInfo()
        if (mGradleConfig.resourceDir.isEmpty()) {
            println("no resources directory found,compressAllDirectory task cancel!!!")
            return
        }
        if (mGradleConfig.apiKey.isEmpty()) {
            println("no api key set,compressAllDirectory task cancel!!!")
            return
        }
        TinyCompress tinyCompress = new TinyCompress(mGradleConfig)
        List<TinyItemInfo> compressedList = getCompressedPicList()
        List<TinyResult> taskResultList = new ArrayList<>()
        //遍历图片资源文件夹
        for (String directory : mGradleConfig.resourceDir) {
            TinyUtils.printLineSeparator()
            println("start process directory >>> ${directory}")
            File dir = new File(directory)
            if (dir.exists() && dir.isDirectory()) {
                //生成待压缩集合
                List<TaskItemInfo> taskList = generateTaskListForDirectory(dir, compressedList)
                println("task list size =  ${taskList.size()} >>> ${directory}")
                TinyUtils.printLineSeparator()
                println("compress task for directory ${directory} start now!!!")
                TinyUtils.printLineSeparator()
                TaskExecutor executor = new TaskExecutor(taskList, mTimeout, tinyCompress)
                TinyResult result = executor.execute()
                taskResultList.add(result)
                TinyUtils.printLineSeparator()
                println("task for directory ${directory} completed compress count ${result.compressedList.size()}/${taskList.size()}!!!")
                TinyUtils.printLineSeparator()
                if (!result.continueNext) {
                    break
                }
            } else {
                if (!dir.exists()) {
                    println("${directory} is not exists!!! Skip it!!!")
                } else if (!dir.isDirectory()) {
                    println("${directory} is not directory!!! Skip it!!!")
                } else {
                    println("${directory} this situation would never happen!!!")
                }
            }
        }
        handleResult(compressedList, taskResultList)
    }

    List<TaskItemInfo> generateTaskListForDirectory(File directory, List<TinyItemInfo> compressedList) {
        List<TaskItemInfo> taskList = new ArrayList<>()
        File[] files = directory.listFiles()
        if (files != null && files.length > 0) {
            int itemIndex = -1
            loopLabel:
            for (int i = 0; i < files.length; i++) {
                File f = files[i]
                //小文件过滤
                long fileSize = f.length()
                if (fileSize <= mIgnoreFileSize) {
                    println("ignore small pic file >>> ${f.path} , size = ${fileSize} byte")
                    continue loopLabel
                }
                //.9图过滤
                if (f.name.contains(".9.")) {
                    println("skip .9 pic >>> ${f.path}")
                    continue loopLabel
                }
                //白名单过滤
                for (String white : mGradleConfig.whiteList) {
                    if (f.name ==~ /$white/) {
                        println("match whit list, skip it >>> ${f.path}")
                        continue loopLabel
                    }
                }
                //已压缩过滤
                for (TinyItemInfo info : compressedList) {
                    if (f.path == info.path && TinyUtils.generateFileMD5(f) == info.md5) {
                        println("pic has already been compressed, skip it>>> ${f.path}")
                        continue loopLabel
                    }
                }
                if (f.name.endsWith(".jpg") || f.name.endsWith(".jpeg") || f.name.endsWith(".png")) {
                    itemIndex++
                    taskList.add(new TaskItemInfo(itemIndex, fileSize, f.name, f.path, f.absolutePath))
                } else {
                    println("wrong pic file format, skip it>>> ${f.path}")
                }
            }
        } else {
            println("${directory} is empty!!! Skip it!!!")
        }
        return taskList
    }

    List<TinyItemInfo> getCompressedPicList() {
        List<TinyItemInfo> compressedList = new ArrayList<>()
        File compressedPicLogFile = new File(mCompressedLogFile)
        if (compressedPicLogFile.exists()) {
            try {
                def parsedList = new JsonSlurper().parse(compressedPicLogFile, TinyConstant.UTF8)
                if (parsedList instanceof ArrayList) {
                    compressedList = parsedList
                } else {
                    println("log file is invalid")
                }
            } catch (Exception ignored) {
                println("log file is invalid")
            }
        }
        return compressedList
    }

    void handleResult(List<TinyItemInfo> compressedList, List<TinyResult> resultList) {
        List<TinyItemInfo> newCompressedList = new ArrayList<>()
        long beforeSize = 0
        long afterSize = 0
        if (resultList) {
            for (TinyResult r : resultList) {
                beforeSize += r.rawSize
                afterSize += r.compressedSize
                newCompressedList.addAll(r.compressedList)
            }
        }
        if (newCompressedList) {
            for (TinyItemInfo info : newCompressedList) {
                int index = compressedList.path.indexOf(info.path)
                if (index >= 0) {
                    compressedList[index] = info
                } else {
                    compressedList.add(0, info)
                }
            }
            JsonOutput jsonOutput = new JsonOutput()
            String json = jsonOutput.toJson(compressedList)
            File logFile = new File(mCompressedLogFile)
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            logFile.write(jsonOutput.prettyPrint(json), TinyConstant.UTF8)
            TinyUtils.printLineSeparator(4)
            println("All task completed, compressAllDirectory file count = ${newCompressedList.size()}, before total size = ${TinyUtils.formatFileSize(beforeSize)} after total size = ${TinyUtils.formatFileSize(afterSize)}")
        }

    }

}