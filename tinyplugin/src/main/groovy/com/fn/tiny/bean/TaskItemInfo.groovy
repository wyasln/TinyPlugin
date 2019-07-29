package com.fn.tiny.bean

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

    TaskItemInfo(int index, long rawSize, String fileName, String filePath, String fileAbsolutePath) {
        this.index = index
        this.rawSize = rawSize
        this.fileName = fileName
        this.filePath = filePath
        this.fileAbsolutePath = fileAbsolutePath
    }

    @Override
    String toString() {
        return "index=${index},rawSize=${rawSize},fileName=${fileName},filePath=${filePath},fileAbsolutePath=${fileAbsolutePath}"
    }

}
