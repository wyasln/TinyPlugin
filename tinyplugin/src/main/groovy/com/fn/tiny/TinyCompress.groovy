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

    static TinyItemInfo performCompress(String absolutePath) {
        File f = new File(absolutePath)
        FileInputStream fis = new FileInputStream(f)
        int beforeSize = fis.available()
        String beforeSizeStr = TinyUtils.formatFileSize(beforeSize)
        Source tSource = Tinify.fromFile(absolutePath)
        tSource.toFile(absolutePath)
        int afterSize = fis.available()
        String afterSizeStr = TinyUtils.formatFileSize(afterSize)
        TinyItemInfo info = new TinyItemInfo(f.path, beforeSizeStr, afterSizeStr, TinyUtils.generateFileMD5(f))
        println("compress pic success, rawSize: $beforeSizeStr -> compressedSize: ${afterSizeStr}")
        return info
    }

}
