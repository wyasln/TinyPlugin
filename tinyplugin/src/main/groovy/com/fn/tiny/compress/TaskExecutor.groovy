package com.fn.tiny.compress


import com.fn.tiny.TinyConstant
import com.fn.tiny.TinyUtils
import com.fn.tiny.bean.DirectoryItemInfo
import com.fn.tiny.bean.CompressInfoWrapper
import com.fn.tiny.bean.FaultInfo
import com.fn.tiny.bean.TaskItemInfo
import com.fn.tiny.bean.CompressedItemInfo

import java.util.concurrent.*

/**
 * TaskExecutor
 * <p>
 * Author xy
 * Date 2019/7/27
 */
class TaskExecutor {

    private List<CompressedItemInfo> mTaskList

    private int mTimeoutCount = 10
    private int mConnectionRetryCount = 10
    private long mTimeout
    private ExecutorService mExecutorService
    private TinyCompress mTinyCompress

    TaskExecutor(List<TaskItemInfo> taskList, long timeout, TinyCompress compress) {
        mTaskList = taskList
        mTimeout = timeout
        mTinyCompress = compress
    }

    DirectoryItemInfo execute() {
        long totalRawSize = 0
        long totalCompressedSize = 0
        List<CompressedItemInfo> compressedList = new ArrayList<>()
        List<FaultInfo> failedList = new ArrayList<>()
        if (mTaskList.isEmpty()) {
            return new DirectoryItemInfo(totalRawSize, totalCompressedSize, compressedList, failedList, true)
        }
        int taskSize = mTaskList.size()
        for (int i = 0; i < taskSize; i++) {
            TaskItemInfo taskItemInfo = mTaskList.get(i)
            checkThreadPool()
            println("start compress pic ${(i + 1)}/${taskSize} >>> ${taskItemInfo.filePath}")
            Future<CompressInfoWrapper> future = mExecutorService.submit(new InnerRunner(taskItemInfo))
            try {
                CompressInfoWrapper infoWrapper = future.get(mTimeout, TimeUnit.SECONDS)
                switch (infoWrapper.tinyStatus) {
                    case TinyConstant.TASK_NORMAL:
                        if (infoWrapper.tinyItemInfo != null) {
                            totalRawSize += infoWrapper.rawSize
                            totalCompressedSize += infoWrapper.compressedSize
                            compressedList.add(infoWrapper.tinyItemInfo)
                            println("compress pic success, rawSize: ${TinyUtils.formatFileSize(infoWrapper.rawSize)} -> compressedSize: ${TinyUtils.formatFileSize(infoWrapper.compressedSize)}")
                        } else {
                            println("could no get result for ${taskItemInfo.filePath}")
                            failedList.add(new FaultInfo(taskItemInfo.filePath, "could no get result"))
                        }
                        break
                    case TinyConstant.TASK_CHANGE_KEY:
                        //更换tiny key，重试
                        i--
                        break
                    case TinyConstant.TASK_KEY_FAULT:
                        //已经没有可用的key,任务终止
                        return new DirectoryItemInfo(totalRawSize, totalCompressedSize, compressedList, failedList, false)
                    case TinyConstant.TASK_CLIENT_FAULT:
                        //客户端错误，任务终止
                        return new DirectoryItemInfo(totalRawSize, totalCompressedSize, compressedList, failedList, false)
                    case TinyConstant.TASK_SERVER_FAULT:
                        //服务端错误,任务终止
                        return new DirectoryItemInfo(totalRawSize, totalCompressedSize, compressedList, failedList, false)
                    case TinyConstant.TASK_CONNECTION_FAULT:
                        //连接tiny服务器异常，重试
                        println("connection retry count = ${mConnectionRetryCount}")
                        if (mConnectionRetryCount > 0) {
                            mConnectionRetryCount--
                            i--
                        }
                        mExecutorService.shutdownNow()
                        break
                    case TinyConstant.TASK_IO_FAULT:
                        //IO异常，记录(可能导致图片损坏),继续下一个
                        failedList.add(new FaultInfo(taskItemInfo.filePath, "Tiny IOException"))
                        break
                    case TinyConstant.TASK_UNKNOWN_FAULT:
                        //未知异常，继续下一个文件夹
                        failedList.add(new FaultInfo(taskItemInfo.filePath, "Tiny Unknown fault"))
                        return new DirectoryItemInfo(totalRawSize, totalCompressedSize, compressedList, failedList, true)
                }

            } catch (InterruptedException e) {
                println("InterruptedException occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                mExecutorService.shutdownNow()
                failedList.add(new FaultInfo(taskItemInfo.filePath, "ExecutorService InterruptedException"))
            } catch (TimeoutException e) {
                failedList.add(new FaultInfo(taskItemInfo.filePath, "ExecutorService TimeoutException"))
                println("TimeoutException occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                println("ExecutorService timeout count = ${mTimeoutCount}")
                if (mTimeoutCount > 0) {
                    mTimeoutCount--
                    mExecutorService.shutdownNow()
                } else {
                    return new DirectoryItemInfo(totalRawSize, totalCompressedSize, compressedList, failedList, true)
                }
            } catch (Exception e) {
                failedList.add(new FaultInfo(taskItemInfo.filePath, "ExecutorService Exception"))
                println("Exception occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath} \n ${e.printStackTrace()}")
                if (mTimeoutCount > 0) {
                    mTimeoutCount--
                    mExecutorService.shutdownNow()
                } else {
                    return new DirectoryItemInfo(totalRawSize, totalCompressedSize, compressedList, failedList, true)
                }
            }
        }
        try {
            mExecutorService.shutdownNow()
        } catch (Exception e) {
            println("shutdown ExecutorService exception!!!")
        }
        return new DirectoryItemInfo(totalRawSize, totalCompressedSize, compressedList, failedList, true)
    }

    private void checkThreadPool() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(1)
        }
        if (mExecutorService.isShutdown() || mExecutorService.isTerminated()) {
            mExecutorService = Executors.newFixedThreadPool(1)
        }
    }

    class InnerRunner implements Callable<CompressInfoWrapper> {

        private TaskItemInfo taskInfo

        InnerRunner(TaskItemInfo info) {
            taskInfo = info
        }

        @Override
        CompressInfoWrapper call() throws Exception {
            return mTinyCompress.performCompress(taskInfo)
        }
    }


}
