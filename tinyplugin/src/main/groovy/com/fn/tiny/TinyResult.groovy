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
    boolean continueNext

    TinyResult() {}

    TinyResult(long beforeSize, long afterSize, List<TinyItemInfo> compressedList, boolean continueNext) {
        this.rawSize = beforeSize
        this.compressedSize = afterSize
        this.compressedList = compressedList
        this.continueNext = continueNext
    }

}