package com.fn.tiny


import com.tinify.ClientException
import com.tinify.ConnectionException
import com.tinify.ServerException

import java.util.concurrent.*

/**
 * TaskExecutor
 * <p>
 * Author xy
 * Date 2019/7/27
 */
class TaskExecutor {

    private List<TinyItemInfo> mTaskList

    private long mRetryCount = 20
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
            return new TinyResult(0, 0, new ArrayList<TinyItemInfo>(), TinyConstant.TASK_NORMAL)
        }
        long totalRawSize = 0
        long totalCompressedSize = 0
        List<TinyItemInfo> compressedList = new ArrayList<>()
        int taskSize = mTaskList.size()
        for (int i = 0; i < taskSize; i++) {
            TaskItemInfo taskItemInfo = mTaskList.get(i)
            checkThreadPool()
            Future<TinyItemInfo> future = mExecutorService.submit(new InnerRunner(taskItemInfo))
            try {
                TinyItemInfo tinyItemInfo = future.get(mTimeout, TimeUnit.SECONDS)
                if (tinyItemInfo != null) {
                    totalRawSize += tinyItemInfo.rawSize
                    totalCompressedSize = tinyItemInfo.compressedSize
                    compressedList.add(tinyItemInfo)
                } else {
                    println("could no get result for ${taskItemInfo.filePath}")
                }
            } catch (InterruptedException e) {
                println("InterruptedException occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                mExecutorService.shutdownNow()
            } catch (TimeoutException e) {
                println("TimeoutException occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                mExecutorService.shutdownNow()
            } catch (ClientException e) {
                println("ClientException occured while comressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                if (mTinyCompress.loopApiKey(false) && mRetryCount > 0) {
                    mRetryCount--
                    i--
                } else {
                    return new TinyResult(totalRawSize, totalCompressedSize, compressedList, TinyConstant.TASK_CLIENT_FAULT)
                }
            } catch (ServerException e) {
                println("ServerException occured while comressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                return new TinyResult(totalRawSize, totalCompressedSize, compressedList, TinyConstant.TASK_SERVER_FAULT)
            } catch (ConnectionException e) {
                println("ConnectionException occured while comressing ${i}/${taskSize} ${taskItemInfo.filePath}")
                if (mRetryCount > 0) {
                    mRetryCount--
                    i--
                } else {
                    return new TinyResult(totalRawSize, totalCompressedSize, compressedList, TinyConstant.TASK_CONNECTION_FAULT)
                }
            } catch (IOException e) {
                println("IOException occured while comressing ${i}/${taskSize} ${taskItemInfo.filePath}")
            } catch (Exception e) {
                println("Exception occured while compressing ${i}/${taskSize} ${taskItemInfo.filePath} \n ${e.printStackTrace()}")
                mExecutorService.shutdownNow()
            }
        }
        try {
            mExecutorService.shutdownNow()
        } catch (Exception e) {
            println("shutdown ExecutorService exception!!!")
        }
        return new TinyResult(totalRawSize, totalCompressedSize, compressedList, TinyConstant.TASK_NORMAL)
    }

    private void checkThreadPool() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(1)
        }
        if (mExecutorService.isShutdown() || mExecutorService.isTerminated()) {
            mExecutorService = Executors.newFixedThreadPool(1)
        }
    }

    class InnerRunner implements Callable<TinyItemInfo> {

        private TaskItemInfo mTaskInfo

        InnerRunner(TaskItemInfo info) {
            mTaskInfo = info
        }

        @Override
        TinyItemInfo call() throws Exception {
            return mTinyCompress.performCompress(mTaskInfo.fileAbsolutePath)
        }
    }


}
