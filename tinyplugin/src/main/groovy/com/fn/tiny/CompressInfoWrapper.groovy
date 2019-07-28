package com.fn.tiny;

/**
 * CompressInfoWrapper
 * <p>
 * Author xy
 * Date 2019/7/29
 */
class CompressInfoWrapper {

    public int tinyStatus
    public TinyItemInfo tinyItemInfo

    public long rawSize
    public long compressedSize

    CompressInfoWrapper(int tinyStatus, TinyItemInfo tinyItemInfo) {
        this.tinyStatus = tinyStatus
        this.tinyItemInfo = tinyItemInfo
    }

    void setSizeInfo(long rawSize, long compressedSize) {
        this.rawSize = rawSize
        this.compressedSize = compressedSize
    }

}
