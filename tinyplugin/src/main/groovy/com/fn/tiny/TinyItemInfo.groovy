package com.fn.tiny

/**
 * TinyItemInfo
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyItemInfo {

    String path
    String rawSize
    String compressedSize
    String md5

    TinyItemInfo() {}

    TinyItemInfo(String path, String preSize, String postSize, String md5) {
        this.path = path
        this.rawSize = preSize
        this.compressedSize = postSize
        this.md5 = md5
    }

}