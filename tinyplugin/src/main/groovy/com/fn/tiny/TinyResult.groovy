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
    ArrayList<TinyItemInfo> compressedList
    //上一个任务执行状况 0 正常  -1 无可用key  -2 ClientException   -3 ServerException  -4 ConnectionException
    int preTaskStatus

    TinyResult() {}

    TinyResult(long beforeSize, long afterSize, List<TinyItemInfo> compressedList, int preTaskStatus) {
        this.rawSize = beforeSize
        this.compressedSize = afterSize
        this.compressedList = compressedList
        this.preTaskStatus = preTaskStatus
    }

}