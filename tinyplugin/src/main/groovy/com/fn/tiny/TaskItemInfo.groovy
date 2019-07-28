package com.fn.tiny

/**
 * TaskItemInfo
 * <p>
 * Author mmmy
 * Date 2019/7/26
 */
class TaskItemInfo {

    public int index
    public long rawSize
    public String fileName
    public String filePath
    public String fileAbsolutePath

    //是否成功压缩
    public boolean success
    //压缩后的大小
    public long compressedSize
    //压缩后的文件MD5
    public String fileMD5

    TaskItemInfo(int index, long rawSize, String fileName, String filePath, String fileAbsolutePath) {
        this.index = index
        this.rawSize = rawSize
        this.fileName = fileName
        this.filePath = filePath
        this.fileAbsolutePath = fileAbsolutePath
    }

    void setCompressInfo(boolean success, long compressedSize, String fileMD5) {
        this.success = success
        this.compressedSize = compressedSize
        this.fileMD5 = fileAbsolutePath
    }

    @Override
    String toString() {
        return "index=${index},rawSize=${rawSize},fileName=${fileName},filePath=${filePath},fileAbsolutePath=${fileAbsolutePath}"
    }

}
