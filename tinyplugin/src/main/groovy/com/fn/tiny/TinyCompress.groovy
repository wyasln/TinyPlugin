package com.fn.tiny

import com.tinify.*

import java.lang.Exception

/**
 * TinyCompress
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyCompress {

    private TinyGradleConfig mGradleConfig
    //当前使用的key
    private String mCurrentKey

    TinyCompress(TinyGradleConfig gradleConfig) {
        mGradleConfig = gradleConfig
        loopApiKey(true)
    }

    boolean loopApiKey(boolean init) {
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
                TinyUtils.printLineSeparator()
                println("API key info ${mCurrentKey} >>> ${Tinify.compressionCount()}/500")
                return true
            }
        }
        println("No useful api key!!! Task must cancel!!!")
        return false
    }

    static boolean setApiKey(String key) {
        try {
            Tinify.setKey(key)
            Tinify.validate()
            return true
        } catch (Exception e) {
            e.printStackTrace()
        }
        return false
    }

    CompressInfoWrapper performCompress(TaskItemInfo taskItemInfo) {
        File f = new File(taskItemInfo.fileAbsolutePath)
        try {
            FileInputStream fis = new FileInputStream(f)
            int beforeSize = fis.available()
            String beforeSizeStr = TinyUtils.formatFileSize(beforeSize)
            Source tSource = Tinify.fromFile(taskItemInfo.fileAbsolutePath)
            tSource.toFile(taskItemInfo.fileAbsolutePath)
            int afterSize = fis.available()
            String afterSizeStr = TinyUtils.formatFileSize(afterSize)
            TinyItemInfo info = new TinyItemInfo(taskItemInfo.filePath, beforeSizeStr, afterSizeStr, TinyUtils.generateFileMD5(f))
            println("compress pic success, rawSize: $beforeSizeStr -> compressedSize: ${afterSizeStr}")
            CompressInfoWrapper wrapper = new CompressInfoWrapper(TinyConstant.TASK_NORMAL, info)
            wrapper.setSizeInfo(beforeSize, afterSize)
            return wrapper
        } catch (AccountException e) {
            println("Tiny AccountException occured while comressing ${taskItemInfo.filePath}")
            if (loopApiKey(false)) {
                return new CompressInfoWrapper(TinyConstant.TASK_CHANGE_KEY, null)
            } else {
                return new CompressInfoWrapper(TinyConstant.TASK_KEY_FAULT, null)
            }
        } catch (ClientException e) {
            println("Tiny ClientException occured while comressing  ${taskItemInfo.filePath}")
            return new CompressInfoWrapper(TinyConstant.TASK_CLIENT_FAULT, null)
        } catch (ServerException e) {
            println("Tiny ServerException occured while comressing  ${taskItemInfo.filePath}")
            return new CompressInfoWrapper(TinyConstant.TASK_SERVER_FAULT, null)
        } catch (ConnectionException e) {
            println("Tiny ConnectionException occured while comressing ${taskItemInfo.filePath}")
            return new CompressInfoWrapper(TinyConstant.TASK_CONNECTION_FAULT, null)
        } catch (IOException e) {
            println("Tiny IOException occured while comressing ${taskItemInfo.filePath}")
            return new CompressInfoWrapper(TinyConstant.TASK_IO_FAULT, null)
        } catch (Exception e) {
            println("Tiny Exception occured while compressing ${taskItemInfo.filePath} \n ${e.printStackTrace()}")
            return new CompressInfoWrapper(TinyConstant.TASK_UNKNOWN_FAULT, null)
        }
    }

}
