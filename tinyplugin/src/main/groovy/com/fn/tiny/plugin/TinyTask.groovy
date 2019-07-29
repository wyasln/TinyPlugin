package com.fn.tiny.plugin

import com.fn.tiny.TinyConstant
import com.fn.tiny.bean.DirectoryItemInfo
import com.fn.tiny.TinyUtils
import com.fn.tiny.bean.FaultInfo
import com.fn.tiny.bean.TaskItemInfo
import com.fn.tiny.bean.CompressedItemInfo
import com.fn.tiny.compress.TaskExecutor
import com.fn.tiny.compress.TinyCompress
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
    String mCompressedLogFilePath
    String mFailedLogFileDirPath
    long mIgnoreFileSize
    long mTimeout

    TinyTask() {
        description = TinyConstant.TASK_DESCRIPTION
        group = TinyConstant.TASK_GROUP
        outputs.upToDateWhen { false }
        android = project.extensions.android
        mGradleConfig = project.tinyConfig
        mCompressedLogFilePath = "${project.projectDir}${File.separator}${mGradleConfig.logFileName}"
        mFailedLogFileDirPath = "${project.projectDir}${File.separator}build${File.separator}${TinyConstant.FAIL_LOG_FILE_DIR}"
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
        //已经压缩过的图片记录
        List<CompressedItemInfo> compressedList = getCompressedPicList()
        List<DirectoryItemInfo> taskResultList = new ArrayList<>()
        //遍历图片资源文件夹
        for (String directory : mGradleConfig.resourceDir) {
            TinyUtils.printLineSeparator()
            println("start process directory >>> ${directory}")
            File dir = new File(directory)
            if (dir.exists() && dir.isDirectory()) {
                //生成待压缩集合
                List<TaskItemInfo> taskList = generateTaskListForDirectory(dir, compressedList)
                if (taskList.isEmpty()) {
                    println("${directory} is with no need for compress , so skip it")
                    continue
                }
                println("task list size =  ${taskList.size()} >>> ${directory}")
                TinyUtils.printLineSeparator()
                println("compress task for directory ${directory} start now!!!")
                TinyUtils.printLineSeparator()
                TaskExecutor executor = new TaskExecutor(taskList, mTimeout, tinyCompress)
                DirectoryItemInfo result = executor.execute()
                taskResultList.add(result)
                TinyUtils.printLineSeparator()
                println("task for directory ${directory} completed , compress count ${result.compressedList.size()}/${taskList.size()}!!!")
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

    List<TaskItemInfo> generateTaskListForDirectory(File directory, List<CompressedItemInfo> compressedList) {
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
                for (CompressedItemInfo info : compressedList) {
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

    List<CompressedItemInfo> getCompressedPicList() {
        List<CompressedItemInfo> compressedList = new ArrayList<>()
        File compressedPicLogFile = new File(mCompressedLogFilePath)
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

    void handleResult(List<CompressedItemInfo> compressedList, List<DirectoryItemInfo> resultList) {
        List<CompressedItemInfo> newCompressedList = new ArrayList<>()
        List<FaultInfo> faultItemList = new ArrayList<>()
        long beforeSize = 0
        long afterSize = 0
        if (resultList) {
            for (DirectoryItemInfo r : resultList) {
                beforeSize += r.rawSize
                afterSize += r.compressedSize
                newCompressedList.addAll(r.compressedList)
                faultItemList.addAll(r.failedList)
            }
        }
        //写入压缩记录文件
        if (newCompressedList) {
            for (CompressedItemInfo info : newCompressedList) {
                int index = compressedList.path.indexOf(info.path)
                if (index >= 0) {
                    compressedList[index] = info
                } else {
                    compressedList.add(0, info)
                }
            }
            JsonOutput jsonOutput = new JsonOutput()
            String json = jsonOutput.toJson(compressedList)
            File logFile = new File(mCompressedLogFilePath)
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            logFile.write(jsonOutput.prettyPrint(json), TinyConstant.UTF8)
            println("write compressed record log file >>> ${mCompressedLogFilePath}")
        }
        //写入错误记录文件
        if (faultItemList) {
            File failLogDir = new File(mFailedLogFileDirPath)
            failLogDir.mkdirs()
            File logFile = new File(failLogDir, TinyConstant.FAIL_LOG_FILE_NAME)
            if (logFile.exists()) {
                logFile.delete()
            }
            logFile.createNewFile()
            JsonOutput jsonOutput = new JsonOutput()
            String json = jsonOutput.toJson(faultItemList)
            logFile.write(jsonOutput.prettyPrint(json), TinyConstant.UTF8)
            println("write compress fail log  file >>> ${mFailedLogFileDirPath}")
        }

        TinyUtils.printLineSeparator(2)
        println("-------------------------------  All task completed  -------------------------------")
        println("directory count = ${mGradleConfig.resourceDir.size()}")
        println("compress success pic count = ${newCompressedList.size()}")
        println("compress fail pic count = ${faultItemList.size()}")
        println("before total size = ${TinyUtils.formatFileSize(beforeSize)} , after total size = ${TinyUtils.formatFileSize(afterSize)}")
    }

}