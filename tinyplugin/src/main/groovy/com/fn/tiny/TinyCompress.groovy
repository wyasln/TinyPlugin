package com.fn.tiny

import com.tinify.*

import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TinyCompress
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyCompress {

    //项目文件路径
    private String mRootPath
    //压缩配置
    private TinyGradleConfig mGradleConfig

    //当前使用的key
    private String mCurrentKey
    //当前key状态，是否有效
    private AtomicBoolean mAtomicBooleanKeyValid
    //忽略文件尺寸
    private long mIgnoreSize

    TinyCompress(String rootPath, TinyGradleConfig config) {
        mRootPath = rootPath
        mGradleConfig = config
        mIgnoreSize = mGradleConfig.ignoreThreshold * 1024
        mAtomicBooleanKeyValid = new AtomicBoolean(false)
        loopApiKey(true)
    }

    private boolean loopApiKey(boolean init) {
        if (mGradleConfig.apiKey.isEmpty()) {
            println("no api key found!!! Task failed!!!")
            mAtomicBooleanKeyValid.set(false)
            return false
        }
        //key依然有效
        if (mAtomicBooleanKeyValid.get()) return true
        int currentIndex = mGradleConfig.apiKey.indexOf(mCurrentKey)
        for (int i = currentIndex + 1; i < mGradleConfig.apiKey.size(); i++) {
            String newKey = mGradleConfig.apiKey.get(i)
            if (setApiKey(newKey)) {
                if (!init) {
                    TinyUtils.printLineSeparator()
                    println("Change api key, new key is ${newKey}, ${i}/${mGradleConfig.apiKey.size()}, old key is ${mCurrentKey}")
                    TinyUtils.printLineSeparator()
                }
                mCurrentKey = newKey
                mAtomicBooleanKeyValid.set(true)
                return true
            }
        }
        mAtomicBooleanKeyValid.set(false)
        println("No useful api key!!! Task must cancel!!!")
        return false
    }

    private static boolean setApiKey(String key) {
        try {
            Tinify.setKey(key)
            Tinify.validate()
            TinyUtils.printLineSeparator()
            println("API key info ${key} >>> ${Tinify.compressionCount()}/500")
            TinyUtils.printLineSeparator()
            return true
        } catch (Exception e) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 调用TinyPng方法进行压缩
     * @param resDir 资源文件夹
     * @param compressedList 已经压缩文件集合
     * @return 压缩结果
     */
    TinyResult compressAllDirectory(File resDir, List<TinyItemInfo> compressedList) {
        int taskExecuteStatus = 0
        long beforeTotalSize = 0
        long afterTotalSize = 0
        List<TinyItemInfo> newCompressedList = new ArrayList<>()
        File[] targetFileArray = resDir.listFiles()
        if (targetFileArray == null || targetFileArray.length == 0) {
            println("Empty sources directory >>> ${resDir.absolutePath}")
            return new TinyResult(beforeTotalSize, afterTotalSize, newCompressedList, taskExecuteStatus)
        }
        int fileCount = targetFileArray.length
        loopLabel:
        for (int i = 0; i < fileCount; i++) {
            File file = targetFileArray[i]
            String filePath = file.path
            String fileName = file.name
            //小文件过滤
            if (file.length() <= mIgnoreSize) {
                println("ignore small pic file >>> ${filePath} >>> fileSize = ${file.length()}")
                continue loopLabel
            }
            //白名单过滤
            for (String s : mGradleConfig.whiteList) {
                if (fileName ==~ /$s/) {
                    println("match whit list, skip it >>> $filePath")
                    continue loopLabel
                }
            }
            //已压缩过滤
            for (TinyItemInfo info : compressedList) {
                if (filePath == info.path && TinyUtils.generateFileMD5(file) == info.md5) {
                    println("Pic has already been compressed, skip it>>> ${info.path}")
                    continue loopLabel
                }
            }
            //.9图过滤
            if (fileName.endsWith(".9.png") || fileName.endsWith(".9.jpg") || fileName.endsWith(".9.jpeg")) {
                println("skip .9 pic >>> ${file.absolutePath}")
                continue loopLabel
            }
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                println("start compress pic >>> ${file.absolutePath}")
                def fis = new FileInputStream(file)
                try {
                    def beforeSize = fis.available()
                    def beforeSizeStr = TinyUtils.formatFileSize(beforeSize)
                    def tempFilePath = "${mRootPath}${File.separator}${resDir}${File.separator}${fileName}"
                    Source tSource = Tinify.fromFile(tempFilePath)
                    tSource.toFile(tempFilePath)
                    def afterSize = fis.available()
                    def afterSizeStr = TinyUtils.formatFileSize(afterSize)
                    beforeTotalSize += beforeSize
                    afterTotalSize += afterSize
                    newCompressedList.add(new TinyItemInfo(filePath, beforeSizeStr, afterSizeStr, TinyUtils.generateFileMD5(file)))
                    println("compress pic success, rawSize: $beforeSizeStr -> compressedSize: ${afterSizeStr}")
                } catch (AccountException e) {
                    //账户异常，是否超出免费图片数量限制
                    TinyUtils.printLineSeparator()
                    println("AccountException: ${e.getMessage()}")
                    mAtomicBooleanKeyValid.set(false)
                    //启用新的key
                    if (loopApiKey(false)) {
                        //重新压缩当前图片
                        i--
                        continue loopLabel
                    } else {
                        //没有可用的key退出
                        taskExecuteStatus = -1
                        break
                    }
                } catch (ClientException e) {
                    println("ClientException: ${e.getMessage()}")
                    //结束task
                    taskExecuteStatus = -2
                    break
                } catch (ServerException e) {
                    println("ServerException: ${e.getMessage()}")
                    //结束task
                    taskExecuteStatus = -3
                    break
                } catch (ConnectionException e) {
                    println("ConnectionException: ${e.getMessage()}")
                    //结束task
                    taskExecuteStatus = -4
                    break
                } catch (IOException e) {
                    println("IOException: ${e.getMessage()}")
                } catch (Exception e) {
                    println("Exception: ${e.toString()}")
                }
            }

        }
        return new TinyResult(beforeTotalSize, afterTotalSize, newCompressedList, taskExecuteStatus)
    }

}
