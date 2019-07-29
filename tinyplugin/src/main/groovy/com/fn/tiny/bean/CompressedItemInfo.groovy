package com.fn.tiny.bean

/**
 * CompressedItemInfo
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class CompressedItemInfo {

    String path
    String rawSize
    String compressedSize
    String md5

    CompressedItemInfo() {}

    CompressedItemInfo(String path, String preSize, String postSize, String md5) {
        this.path = path
        this.rawSize = preSize
        this.compressedSize = postSize
        this.md5 = md5
    }

}