package com.fn.tiny


import java.util.concurrent.*

/**
 * TaskExecutor
 * <p>
 * Author xy
 * Date 2019/7/27
 */
class TaskExecutor {

    private List<TinyItemInfo> mTaskList

    private int mTimeoutCount = 20
    private int mConnectionRetryCount = 20
    private long mTimeout
    private ExecutorService mExecutorService
    private TinyCompress mTinyCompress

    TaskExecutor(List<TaskItemInfo> taskList, long timeout, TinyCompress compress) {
        mTaskList = taskList
        mTimeout = timeout
        mTinyCompress = compress
    }

    TinyResult execute() {
        if (mTaskList.isEmpty()) {
            return new TinyResult(0, 0, new ArrayList<TinyItemInfo>(), true)
        }
        long totalRawSize = 0
        long totalCompressedSize = 0
        List<TinyItemInfo> compressedList = new ArrayList<>()
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
                            totalCompressedSize = infoWrapper.compressedSize
                            compressedList.add(infoWrapper.tinyItemInfo)
                        } else {
                            println("could no get result for ${taskItemInfo.filePath}")
                        }
                        break
                    case TinyConstant.TASK_CHANGE_KEY:
                        i--
                        break
                    case TinyConstant.TASK_KEY_FAULT:
                        //已经没有可用的key
                        return new TinyResult(totalRawSize, totalCompressedSize, compressedList, false)
                    case TinyConstant.TASK_CLIENT_FAULT:
                        //客户端错误，任务终止
                        return new TinyResult(totalRawSize, totalCompressedSize, compressedList, false)
                    case TinyConstant.TASK_SERVER_FAULT:
                        //服务端错误,任务终止
                        return new TinyResult(totalRawSize, totalCompressedSize, compressedList, false)
                    case TinyConstant.TASK_CONNECTION_FAULT:
                        if (mConnectionRetryCount > 0) {
                            mConnectionRetryCount--
                            i--
                        }
                        break
                    case TinyConstant.TASK_IO_FAULT:
                        break
                    case TinyConstant.TASK_UNKNOWN_FAULT:
                        return new TinyResult(totalRawSize, totalCompressedSize, compressedList, true)
                }

            } catch (InterruptedException e) {
                println("InterruptedException occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                mExecutorService.shutdownNow()
            } catch (TimeoutException e) {
                println("TimeoutException occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                if (mTimeoutCount > 0) {
                    mTimeoutCount--
                    mExecutorService.shutdownNow()
                } else {
                    return new TinyResult(totalRawSize, totalCompressedSize, compressedList, true)
                }
            } catch (Exception e) {
                println("Exception occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath} \n ${e.printStackTrace()}")
                if (mTimeoutCount > 0) {
                    mTimeoutCount--
                    mExecutorService.shutdownNow()
                } else {
                    return new TinyResult(totalRawSize, totalCompressedSize, compressedList, true)
                }
            }
        }
        try {
            mExecutorService.shutdownNow()
        } catch (Exception e) {
            println("shutdown ExecutorService exception!!!")
        }
        return new TinyResult(totalRawSize, totalCompressedSize, compressedList, true)
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
