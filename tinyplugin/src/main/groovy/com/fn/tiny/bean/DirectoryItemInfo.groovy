package com.fn.tiny.bean
/**
 * DirectoryItemInfo
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class DirectoryItemInfo {

    long rawSize
    long compressedSize
    List<CompressedItemInfo> compressedList
    List<FaultInfo> failedList
    boolean continueNext

    DirectoryItemInfo() {}

    DirectoryItemInfo(long rawSize,
                      long compressedSize,
                      List<CompressedItemInfo> compressedList,
                      List<FaultInfo> failedList,
                      boolean continueNext) {
        this.rawSize = rawSize
        this.compressedSize = compressedSize
        this.compressedList = compressedList
        this.failedList = failedList
        this.continueNext = continueNext
    }
}