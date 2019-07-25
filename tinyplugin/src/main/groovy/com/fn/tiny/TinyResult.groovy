package com.fn.tiny

/**
 * TinyResult
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyResult {

    long rawSize
    long compressedSize
    ArrayList<TinyItemInfo> results
    //是否进行下一个文件夹的压缩(无可用key时为false)
    boolean continueNextDir

    TinyResult(long beforeSize, long afterSize, ArrayList<TinyItemInfo> results, boolean continueNext) {
        this.rawSize = beforeSize
        this.compressedSize = afterSize
        this.results = results
        this.continueNextDir = continueNext
    }

}